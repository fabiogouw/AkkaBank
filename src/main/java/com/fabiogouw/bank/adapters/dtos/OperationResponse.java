package com.fabiogouw.bank.adapters.dtos;

import java.math.BigDecimal;

public class OperationResponse {
    private String _correlationId;
    private BigDecimal _amount;
    private BigDecimal _currentBalance;
    private Boolean _success;

    public OperationResponse() {

    }

    public OperationResponse(String correlationId, BigDecimal amount, BigDecimal currentBalance, Boolean success) {
        _correlationId = correlationId;
        _amount = amount;
        _currentBalance = currentBalance;
        _success = success;
    }

    public String getCorrelationId() {
        return _correlationId;
    }
    public BigDecimal getAmount() {
        return _amount;
    }
    public BigDecimal getCurrentBalance() {
        return _currentBalance;
    }
    public Boolean getSuccess() {
        return _success;
    }    
    public void setCorrelationId(String correlationId){
        _correlationId = correlationId;
    }
    public void setAmount(BigDecimal amount) {
        _amount = amount;
    }
    public void setCurrentBalance(BigDecimal currentBalance) {
        _currentBalance = currentBalance;
    }    
    public void setSuccess(Boolean success) {
        _success = success;
    }
}