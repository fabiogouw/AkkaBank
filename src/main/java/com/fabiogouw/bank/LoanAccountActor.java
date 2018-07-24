package com.fabiogouw.bank;

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

    static class LoanPaymentRequest {
        private String _correlationId;
        private double _availableAmount;

        public LoanPaymentRequest(String correlationId, double availableAmount) {
            _correlationId = correlationId;
            _availableAmount = availableAmount;
        }
        
        public String getCorrelationId() {
            return _correlationId;
        }
        public double getAvailableAmount() {
            return _availableAmount;
        }
    }
    
    static class LoanPaymentResponse {
        private String _correlationId;
        private double _remainingAmount;

        public LoanPaymentResponse(String correlationId, double remainingAmount) {
            _correlationId = correlationId;
            _remainingAmount = remainingAmount;
        }
        
        public String getCorrelationId() {
            return _correlationId;
        }
        public double getRemainingAmount() {
            return _remainingAmount;
        }        
    }     

    static Props props(String id, double maxLoan) {
        return Props.create(LoanAccountActor.class, id, maxLoan);
    }    

    private final LoggingAdapter _log;
    private String _id;
    private final double _maxLoan;
    private double _currentLoan;

    public LoanAccountActor(String id, double maxLoan) {
        _id = id;
        _maxLoan = maxLoan;
        _log = Logging.getLogger(getContext().getSystem(), this);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(LoanRequest.class, req -> {
                _log.info("LoanRequest - {}, {}, {}", req.getAmountRequested(), _maxLoan, _currentLoan);
                double available = _maxLoan - _currentLoan;
                if(available >= req.getAmountRequested()) {
                    _currentLoan = _currentLoan + req.getAmountRequested();
                    getSender().tell(new LoanResponse(req.getCorrelationId(), req.getAmountRequested(), true), getSelf());
                }
                else {
                    getSender().tell(new LoanResponse(req.getCorrelationId(), 0, false), getSelf());
                }                
            })
            .match(LoanPaymentRequest.class, req -> {
                double usedAmount = 0;
                if(_currentLoan > req.getAvailableAmount()) {
                    usedAmount = req.getAvailableAmount();
                    _currentLoan = _currentLoan - usedAmount;                    
                }
                else {
                    usedAmount = _currentLoan;
                    _currentLoan = 0;
                }
                getSender().tell(new LoanPaymentResponse(req.getCorrelationId(), req.getAvailableAmount() - usedAmount), getSelf());
            })
            .build();
    }
}