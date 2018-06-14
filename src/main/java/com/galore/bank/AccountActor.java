package com.galore.bank;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.Option;

public class AccountActor extends AbstractActor {

    static class BalanceRequest {

    }

    static class BalanceResponse {
        private double _balance;
        
        public BalanceResponse(double balance) {
            _balance = balance;
        }
        public double getBalance() {
            return _balance;
        }
    }

    static class DepositRequest {
        private String _correlationId;
        private double _amount;
        
        public DepositRequest(String correlationId, double amount) {
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

    static class DepositResponse {
        private String _correlationId;
        private double _currentBalance;
        private Boolean _success;
        public DepositResponse(String correlationId, double currentBalance, Boolean success) {
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

    static class WithdrawRequest {
        private String _correlationId;
        private double _amount;
        
        public WithdrawRequest(String correlationId, double amount) {
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

    static class WithdrawResponse {
        private String _correlationId;
        private double _currentBalance;
        private Boolean _success;
        public WithdrawResponse(String correlationId, double currentBalance, Boolean success) {
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
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        super.preRestart(reason, message);
        _balance += _ledger.getBalance(_id);
    }

    @Override
    public void postStop() throws Exception {
        _ledger.saveBalance(_id, new Date(), _balance);
        super.postStop();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(BalanceRequest.class, req -> {
                getSender().tell(new BalanceResponse(_balance), getSelf());
            })
            .match(DepositRequest.class, req -> {
                ActorRef respondTo = getSender();
                _ledger.insert(_id, new Date(), UUID.randomUUID(), req.getAmount(), UUID.fromString(req.getCorrelationId()), "deposit", Ledger.EntryType.DEPOSIT.getValue());
                _entriesInserted++;
                _balance = _balance + req.getAmount();
                respondTo.tell(new DepositResponse(req.getCorrelationId(), _balance, true), getSelf());
                saveBalanceIfNeeded();
            })
            .match(WithdrawRequest.class, req -> {
                _log.info("WithdrawRequest - {}", req.getAmount());
                if(_balance >= req.getAmount()) {
                    _ledger.insert(_id, new Date(), UUID.randomUUID(), req.getAmount(), UUID.fromString(req.getCorrelationId()), "withdraw", Ledger.EntryType.WITHDRAW.getValue());
                    _entriesInserted++;
                    _balance = _balance - req.getAmount();
                    getSender().tell(new WithdrawResponse(req.getCorrelationId(), _balance, true), getSelf());
                    saveBalanceIfNeeded();
                }
                else {
                    getSender().tell(new WithdrawResponse(req.getCorrelationId(), _balance, false), getSelf());
                }
            })
            .build();
    }

    private void saveBalanceIfNeeded() {
        if(_entriesInserted > 5) {
            _ledger.saveBalance(_id, new Date(), _balance);
            _entriesInserted = 0;
        }
    }
}