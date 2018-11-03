package com.fabiogouw.bank.adapters.actors.messages;

import java.io.Serializable;

public class OperationResponse implements Serializable {
    private static final long serialVersionUID = -6747511039799099748L;
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