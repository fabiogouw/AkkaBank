package com.fabiogouw.bank.adapters.actors.messages;

import java.math.BigDecimal;

public class OperationRequest extends AccountMessage {
    private static final long serialVersionUID = -188612147356070992L;
    private final String _correlationId;
    private final BigDecimal _amount;
    
    public OperationRequest(String accountId, String correlationId, BigDecimal amount) {
        super(accountId);
        _correlationId = correlationId;
        _amount = amount;
    }
    public String getCorrelationId() {
        return _correlationId;
    }
    public BigDecimal getAmount() {
        return _amount;
    }
}