package com.galore.bank;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class LoanAccountActor extends AbstractActor {

    static class LoanRequest {
        private String _correlationId;
        private double _amountRequested;

        public LoanRequest(String correlationId, double amountRequested) {
            _correlationId = correlationId;
            _amountRequested = amountRequested;
        }
        
        public String getCorrelationId() {
            return _correlationId;
        }
        public double getAmountRequested() {
            return _amountRequested;
        }
    }

    static class LoanResponse {
        private String _correlationId;
        private double _amount;
        private Boolean _success;

        public LoanResponse(String correlationId, double amount, Boolean success) {
            _correlationId = correlationId;
            _amount = amount;
            _success = success;
        }

        public String getCorrelationId() {
            return _correlationId;
        }
        public double getAmount() {
            return _amount;
        }
        public Boolean getSuccess() {
            return _success;
        }        
    }

    static Props props(String id, double max) {
        return Props.create(LoanAccountActor.class, id, max);
    }    

    private final LoggingAdapter _log;
    private String _id;
    private double _max;
    private double _current;

    public LoanAccountActor(String id, double max) {
        _id = id;
        _max = max;
        _log = Logging.getLogger(getContext().getSystem(), this);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(LoanRequest.class, req -> {
                _log.info("LoanRequest - {}, {}, {}", req.getAmountRequested(), _max, _current);
                double available = _max - _current;
                if(available >= req.getAmountRequested()) {
                    _current = _current + req.getAmountRequested();
                    getSender().tell(new LoanResponse(req.getCorrelationId(), req.getAmountRequested(), true), getSelf());
                }
                else {
                    getSender().tell(new LoanResponse(req.getCorrelationId(), 0, false), getSelf());
                }                
            })
            .build();
    }
}