package com.fabiogouw.bank.adapters.actors.messages;

import java.io.Serializable;
import java.math.BigDecimal;

public class OperationResponse implements Serializable {
    private static final long serialVersionUID = -6747511039799099748L;
    private final String _correlationId;
    private final BigDecimal _currentBalance;
    private final Boolean _success;
    public OperationResponse(String correlationId, BigDecimal currentBalance, Boolean success) {
        _correlationId = correlationId;
        _currentBalance = currentBalance;
        _success = success;
    }

    public String getCorrelationId() {
        return _correlationId;
    }
    public BigDecimal getCurrentBalance() {
        return _currentBalance;
    }        
    public Boolean getSuccess() {
        return _success;
    }
}  