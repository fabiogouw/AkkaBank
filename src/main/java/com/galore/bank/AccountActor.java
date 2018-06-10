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

    static Props props(String id, double initialBalance, Ledger ledger, ActorRef loadAccountRef) {
        return Props.create(AccountActor.class, id, initialBalance, ledger, loadAccountRef);
    }

    private final LoggingAdapter _log;
    private final Ledger _ledger;
    private String _id;
    private double _balance;
    private ActorRef _loadAccountRef;
    private final Map<String, BalanceReservation> _balanceReservations = new HashMap<String, BalanceReservation>();
    private final Map<String, ActorRef> _loanPaymentResponseTo = new HashMap<String, ActorRef>();

    public AccountActor(String id, double initialBalance, Ledger ledger, ActorRef loadAccountRef) {
        _id = id;
        _balance = initialBalance;
        _ledger = ledger;
        _loadAccountRef = loadAccountRef;
        _log = Logging.getLogger(getContext().getSystem(), this);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        _balance += _ledger.getBalance(_id, new Date());
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        super.preRestart(reason, message);
        _balance += _ledger.getBalance(_id, new Date());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(BalanceRequest.class, req -> {
                getSender().tell(new BalanceResponse(_balance), getSelf());
            })
            .match(DepositRequest.class, req -> {
                ActorRef respondTo = getSender();
                if(_loadAccountRef != null) {
                    _loanPaymentResponseTo.put(req.getCorrelationId(), getSender());
                    _loadAccountRef.tell(new LoanAccountActor.LoanPaymentRequest(req.getCorrelationId(), req.getAmount()), getSelf());
                }
                else {
                    _ledger.insert(_id, new Date(), UUID.randomUUID(), req.getAmount(), UUID.fromString(req.getCorrelationId()), "deposit", Ledger.EntryType.DEPOSIT.getValue());
                    _balance = _balance + req.getAmount();
                    respondTo.tell(new DepositResponse(req.getCorrelationId(), _balance, true), getSelf());
                }
            })
            .match(LoanAccountActor.LoanPaymentResponse.class, res -> {
                _balance = _balance + res.getRemainingAmount();
                ActorRef responseTo = _loanPaymentResponseTo.remove(res.getCorrelationId());
                responseTo.tell(new DepositResponse(res.getCorrelationId(), _balance, true), getSelf());
            })
            .match(WithdrawRequest.class, req -> {
                _log.info("WithdrawRequest - {}", req.getAmount());
                if(_balance >= req.getAmount()) {
                    _ledger.insert(_id, new Date(), UUID.randomUUID(), req.getAmount(), UUID.fromString(req.getCorrelationId()), "deposit", Ledger.EntryType.WITHDRAW.getValue());
                    _balance = _balance - req.getAmount();
                    getSender().tell(new WithdrawResponse(req.getCorrelationId(), _balance, true), getSelf());
                }
                else if(_loadAccountRef != null) {
                    double loanRequired = req.getAmount() - _balance;
                    _balanceReservations.put(req.getCorrelationId(), new BalanceReservation(req.getCorrelationId(), _balance, getSender()));
                    _balance = 0;
                    _loadAccountRef.tell(new LoanAccountActor.LoanRequest(req.getCorrelationId(), loanRequired), getSelf());
                }
                else {
                    getSender().tell(new WithdrawResponse(req.getCorrelationId(), _balance, false), getSelf());
                }
            })
            .match(LoanAccountActor.LoanResponse.class, res -> {
                _log.info("LoanAccountActor.LoanResponse - {}, {}", res.getSuccess(), res.getAmount());
                BalanceReservation reservation = _balanceReservations.remove(res.getCorrelationId());
                if(res.getSuccess()) {
                    reservation.getOriginalRequester().tell(new WithdrawResponse(res.getCorrelationId(), _balance, true), getSelf());
                }
                else {                    
                    _balance = _balance + reservation.getAmount();
                    reservation.getOriginalRequester().tell(new WithdrawResponse(res.getCorrelationId(), _balance, false), getSelf());
                }
            })
            .build();
    }
}