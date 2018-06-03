package com.galore.bank;

import java.util.HashMap;
import java.util.Map;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class AccountActor extends AbstractActor {

    static class BalanceRequest {

    }

    static class BalanceResponse {
        private String _id;
        private double _balance;
        
        public BalanceResponse(String id, double balance) {
            _id = id;
            _balance = balance;
        }
        public String getId() {
            return _id;
        }
        public double getBalance() {
            return _balance;
        }
    }

    static class DepositRequest {
        private String _id;
        private double _amount;
        
        public DepositRequest(String id, double amount) {
            _id = id;
            _amount = amount;
        }
        public String getId() {
            return _id;
        }
        public double getAmount() {
            return _amount;
        }
    }

    static class DepositResponse {

    }

    static class WithdrawRequest {
        private String _id;
        private double _amount;
        
        public WithdrawRequest(String id, double amount) {
            _id = id;
            _amount = amount;
        }
        public String getId() {
            return _id;
        }
        public double getAmount() {
            return _amount;
        }
    }

    static class WithdrawResponse {
        private String _id;
        private Boolean _success;
        public WithdrawResponse(String id, Boolean success) {
            _id = id;
            _success = success;
        }

        public String getId() {
            return _id;
        }
        public Boolean getSuccess() {
            return _success;
        }
    }

    static Props props(String id, double initialBalance, ActorRef loadAccountRef) {
        return Props.create(AccountActor.class, id, initialBalance, loadAccountRef);
    }

    private final LoggingAdapter _log;
    private String _id;
    private double _balance;
    private ActorRef _loadAccountRef;
    private final Map<String, BalanceReservation> _balanceReservations = new HashMap<String, BalanceReservation>();

    public AccountActor(String id, double initialBalance, ActorRef loadAccountRef) {
        _id = id;
        _balance = initialBalance;
        _loadAccountRef = loadAccountRef;
        _log = Logging.getLogger(getContext().getSystem(), this);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(BalanceRequest.class, req -> {
                getSender().tell(new BalanceResponse(_id, _balance), getSelf());
            })
            .match(DepositRequest.class, req -> {
                _balance = _balance + req.getAmount();
                getSender().tell(new DepositResponse(), getSelf());
            })
            .match(WithdrawRequest.class, req -> {
                _log.info("Withdraw request received");
                if(_balance >= req.getAmount()) {
                    _balance = _balance - req.getAmount();
                    getSender().tell(new WithdrawResponse(req.getId(), true), getSelf());
                }
                else if(_loadAccountRef != null) {
                    double loanRequired = req.getAmount() - _balance;
                    _balanceReservations.put(req.getId(), new BalanceReservation(req.getId(), _balance, getSender()));
                    _balance = 0;
                    _loadAccountRef.tell(new LoanAccountActor.LoanRequest(req.getId(), loanRequired), getSelf());
                }
                else {
                    getSender().tell(new WithdrawResponse(req.getId(), false), getSelf());
                }
            })
            .match(LoanAccountActor.LoanResponse.class, res -> {
                _log.info("LoanAccountActor.LoanResponse - {}, {}", res.getSuccess(), res.getAmount());
                BalanceReservation reservation = _balanceReservations.remove(res.getCorrelationId());
                if(res.getSuccess()) {
                    reservation.getOriginalRequester().tell(new WithdrawResponse(res.getCorrelationId(), true), getSelf());
                }
                else {                    
                    _balance = _balance + reservation.getAmount();
                    reservation.getOriginalRequester().tell(new WithdrawResponse(res.getCorrelationId(), false), getSelf());
                }
            })
            .build();
    }
}