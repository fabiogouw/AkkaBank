package com.fabiogouw.bank.adapters.dtos;

import java.math.BigDecimal;

public class OperationRequest {
    private String _correlationId;
    private BigDecimal _amount;

    public String getCorrelationId() {
        return _correlationId;
    }
    public BigDecimal getAmount() {
        return _amount;
    }
    public void setCorrelationId(String correlationId){
        _correlationId = correlationId;
    }
    public void setAmount(BigDecimal amount) {
        _amount = amount;
    }
}