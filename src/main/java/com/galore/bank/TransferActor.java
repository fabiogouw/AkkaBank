package com.galore.bank;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.Option;

public class TransferActor extends AbstractActor {

    static class TransferRequest {
        private String _correlationId;
        private String _accountFrom;
        private String _accountTo;
        private double _amount;
        
        public TransferRequest(String correlationId, String accountFrom, String accountTo, double amount) {
            _correlationId = correlationId;
            _accountFrom = accountFrom;
            _accountTo = accountTo;
            _amount = amount;
        }
        public String getCorrelationId() {
            return _correlationId;
        }
        public String getAccountFrom() {
            return _accountFrom;
        }
        public String getAccountTo() {
            return _accountTo;
        }        
        public double getAmount() {
            return _amount;
        }
    }

    static class TransferResponse {
        private String _correlationId;
        private Boolean _success;
        public TransferResponse(String correlationId, Boolean success) {
            _correlationId = correlationId;
            _success = success;
        }

        public String getCorrelationId() {
            return _correlationId;
        }       
        public Boolean getSuccess() {
            return _success;
        }
    }

    static Props props(AccountBag accountBag) {
        return Props.create(TransferActor.class, accountBag);
    }

    private final LoggingAdapter _log;
    private final AccountBag _accountBag;
    private String _correlationId;
    private ActorRef _accountFrom;
    private ActorRef _accountTo;
    private double _amount;
    private ActorRef _originalSender;

    public TransferActor(AccountBag accountBag) {
        _accountBag = accountBag;
        _log = Logging.getLogger(getContext().getSystem(), this);
    }

    private Receive receiveStarting() {
        return receiveBuilder()
        .match(TransferRequest.class, req -> {
            _correlationId = req.getCorrelationId();
            _accountFrom = _accountBag.get(req.getAccountFrom());
            _accountTo = _accountBag.get(req.getAccountTo());
            _amount = req.getAmount();
            _originalSender = getSender();
            _log.info("Transfer for {}, {}, {}, {}", _correlationId, req.getAccountFrom(), req.getAccountTo(), _amount);
            getContext().become(receiveWithdrawing());
            _accountFrom.tell(new AccountActor.WithdrawRequest(_correlationId, _amount), getSelf());
        })
        .build();
    }

    private Receive receiveWithdrawing() {
        return receiveBuilder()
        .match(AccountActor.WithdrawResponse.class, res -> {
            _log.info("Transfer withdraw for {} - {}", _correlationId, res.getSuccess());
            if(res.getSuccess()) {
                getContext().become(receiveDepositing());
                _accountTo.tell(new AccountActor.DepositRequest(_correlationId, _amount), getSelf());
            }
            else {
                _originalSender.tell(new TransferResponse(_correlationId, false), getSelf());
                getSelf().tell(PoisonPill.getInstance(), getSelf());
            }
        })
        .build();
    }

    private Receive receiveDepositing() {
        return receiveBuilder()
        .match(AccountActor.DepositResponse.class, res -> {
            _log.info("Transfer deposit for {} - {}", _correlationId, res.getSuccess());
            _originalSender.tell(new TransferResponse(_correlationId, res.getSuccess()), getSelf());
            getSelf().tell(PoisonPill.getInstance(), getSelf());
        })
        .build();
    }

    @Override
    public Receive createReceive() {
        return receiveStarting();
    }
}