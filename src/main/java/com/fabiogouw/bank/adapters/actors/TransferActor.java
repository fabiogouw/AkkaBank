package com.fabiogouw.bank.adapters.actors;

import com.fabiogouw.bank.adapters.actors.messages.DepositRequest;
import com.fabiogouw.bank.adapters.actors.messages.DepositResponse;
import com.fabiogouw.bank.adapters.actors.messages.WithdrawRequest;
import com.fabiogouw.bank.adapters.actors.messages.WithdrawResponse;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.math.BigDecimal;

public class TransferActor extends AbstractActor {

    static class TransferRequest {
        private String _correlationId;
        private String _accountFrom;
        private String _accountTo;
        private BigDecimal _amount;
        
        public TransferRequest(String correlationId, String accountFrom, String accountTo, BigDecimal amount) {
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
        public BigDecimal getAmount() {
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

    static Props props() {
        return Props.create(TransferActor.class);
    }

    private final LoggingAdapter _log;
    private String _correlationId;
    private String _accountFrom;
    private String _accountTo;
    private BigDecimal _amount;
    private ActorRef _originalSender;

    public TransferActor() {
        _log = Logging.getLogger(getContext().getSystem(), this);
    }

    private Receive receiveStarting() {
        return receiveBuilder()
        .match(TransferRequest.class, req -> {
            _correlationId = req.getCorrelationId();
            _accountFrom = req.getAccountFrom();
            _accountTo = req.getAccountTo();
            _amount = req.getAmount();
            _originalSender = getSender();
            _log.info("Transfer for {}, {}, {}, {}", _correlationId, req.getAccountFrom(), req.getAccountTo(), _amount);
            ActorRef accountRegion = ClusterSharding.get(getContext().getSystem()).shardRegion(AccountActor.SHARD);
            getContext().become(receiveWithdrawing());
            accountRegion.tell(new WithdrawRequest(_accountFrom, _correlationId, _amount), getSelf());
        })
        .build();
    }

    private Receive receiveWithdrawing() {
        return receiveBuilder()
        .match(WithdrawResponse.class, res -> {
            _log.info("Transfer withdraw for {} - {}", _correlationId, res.getSuccess());
            if(res.getSuccess()) {
                ActorRef accountRegion = ClusterSharding.get(getContext().getSystem()).shardRegion(AccountActor.SHARD);
                getContext().become(receiveDepositing());
                accountRegion.tell(new DepositRequest(_accountTo, _correlationId, _amount), getSelf());
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
        .match(DepositResponse.class, res -> {
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