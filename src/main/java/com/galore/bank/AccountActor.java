package com.galore.bank;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.AbstractPersistentActor;
import scala.Option;

import java.util.concurrent.*;

import com.galore.bank.Ledger.EntryType;

public class AccountActor extends AbstractActorWithStash {

    static class BalanceRequest {

    }

    static class BalanceResponse {
        private final double _balance;
        
        public BalanceResponse(double balance) {
            _balance = balance;
        }
        public double getBalance() {
            return _balance;
        }
    }

    static class OperationRequest {
        private final String _correlationId;
        private final double _amount;
        
        public OperationRequest(String correlationId, double amount) {
            _correlationId = correlationId;
            _amount = amount;
        }
        public String getCorrelationId() {
            return _correlationId;
        }
        public double getAmount() {
            return _amount;
        }
    }

    static class OperationResponse {
        private final String _correlationId;
        private final double _currentBalance;
        private final Boolean _success;
        public OperationResponse(String correlationId, double currentBalance, Boolean success) {
            _correlationId = correlationId;
            _currentBalance = currentBalance;
            _success = success;
        }

        public String getCorrelationId() {
            return _correlationId;
        }
        public double getCurrentBalance() {
            return _currentBalance;
        }        
        public Boolean getSuccess() {
            return _success;
        }
    }    

    static class DepositRequest extends OperationRequest {
        public DepositRequest(String correlationId, double amount) {
            super(correlationId, amount);
        }
    }

    static class DepositResponse extends OperationResponse {
        public DepositResponse(String correlationId, double currentBalance, Boolean success) {
            super(correlationId, currentBalance, success);
        }
    }

    static class WithdrawRequest extends OperationRequest {
        public WithdrawRequest(String correlationId, double amount) {
            super(correlationId, amount);
        }
    }

    static class WithdrawResponse extends OperationResponse {
        public WithdrawResponse(String correlationId, double currentBalance, Boolean success) {
            super(correlationId, currentBalance, success);
        }
    }

    static class InternalOperationStateUpdate {
        private final String _correlationId;
        private final Ledger.EntryType _entryType;
        private final double _amount;
        private final ActorRef _respondTo;

        public InternalOperationStateUpdate(String correlationId, Ledger.EntryType entryType, double amount, ActorRef respondTo) {
            _correlationId = correlationId;
            _entryType = entryType;
            _amount = amount;
            _respondTo = respondTo;
        }

        public String getCorrelationId() {
            return _correlationId;
        }        
        public Ledger.EntryType getEntryType() {
            return _entryType;
        }        
        public double getAmount() {
            return _amount;
        }
        public ActorRef getRespondTo() {
            return _respondTo;
        }
    }

    static Props props(String id, double initialBalance, Ledger ledger) {
        return Props.create(AccountActor.class, id, initialBalance, ledger);
    }

    private final LoggingAdapter _log;
    private final Ledger _ledger;
    private String _id;
    private double _balance;
    private int _entriesInserted = 0;

    public AccountActor(String id, double initialBalance, Ledger ledger) {
        _id = id;
        _balance = initialBalance;
        _ledger = ledger;
        _log = Logging.getLogger(getContext().getSystem(), this);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        _balance += _ledger.getBalance(_id);
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) {
        super.preRestart(reason, message);
        _balance += _ledger.getBalance(_id);
    }

    @Override
    
    public void postStop() {
        _ledger.saveBalance(_id, new Date(), _balance);
        super.postStop();
    }

    @Override
    public Receive createReceive() {
        return createRespondingReceive();
    }

    private Receive createUpdatingReceive() {
        return receiveBuilder()
            .match(BalanceRequest.class, req -> {
                // it doesn't matter if we're updating, we can send the current balance
                sendBalance(getSender());
            })        
            .match(InternalOperationStateUpdate.class, upd -> {
                _entriesInserted += 1;
                _balance = _balance + upd.getAmount();
                saveBalanceIfNeeded();
                OperationResponse response = null;
                if(upd.getEntryType() == EntryType.DEPOSIT) {
                    response = new DepositResponse(upd.getCorrelationId(), _balance, true);
                }
                else if(upd.getEntryType() == EntryType.WITHDRAW) {
                    response = new WithdrawResponse(upd.getCorrelationId(), _balance, true);
                }
                upd.getRespondTo().tell(response, getSelf());
                getContext().unbecome();
                unstashAll();
            })
            .matchAny(o -> {
                stash();
            })
            .build();
    }

    private Receive createRespondingReceive() {
        return receiveBuilder()
            .match(BalanceRequest.class, req -> {
                sendBalance(getSender());
            })
            .match(DepositRequest.class, req -> {
                ActorRef respondTo = getSender();
                CompletableFuture<Void> future = _ledger.insert(_id, new Date(), UUID.randomUUID(), req.getAmount(), UUID.fromString(req.getCorrelationId()), "deposit", Ledger.EntryType.DEPOSIT.getValue());
                getContext().become(createUpdatingReceive());
                future.thenAccept(r -> {
                    getSelf().tell(new InternalOperationStateUpdate(req.getCorrelationId(), EntryType.DEPOSIT, req.getAmount(), respondTo), getSelf());
                });
            })
            .match(WithdrawRequest.class, req -> {
                ActorRef respondTo = getSender();
                if(_balance >= req.getAmount()) {
                    CompletableFuture<Void> future = _ledger.insert(_id, new Date(), UUID.randomUUID(), req.getAmount(), UUID.fromString(req.getCorrelationId()), "withdraw", Ledger.EntryType.WITHDRAW.getValue());
                    future.thenAccept(r -> {
                        getSelf().tell(new InternalOperationStateUpdate(req.getCorrelationId(), EntryType.WITHDRAW, req.getAmount(), respondTo), getSelf());
                    });
                }
                else {
                    getSender().tell(new WithdrawResponse(req.getCorrelationId(), _balance, false), getSelf());
                }
            })        
            .build();
    }

    private void sendBalance(ActorRef respondTo) {
        respondTo.tell(new BalanceResponse(_balance), getSelf());
    }

    private void saveBalanceIfNeeded() {
        if(_entriesInserted > 5) {
            _ledger.saveBalance(_id, new Date(), _balance);
            _entriesInserted = 0;
        }
    }
}