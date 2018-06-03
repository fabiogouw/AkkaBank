package com.galore.bank;

public class OperationResponse {
    private String _correlationId;
    private double _amount;
    private double _currentBalance;
    private Boolean _success;

    public OperationResponse() {

    }

    public OperationResponse(String correlationId, double amount, double currentBalance, Boolean success) {
        _correlationId = correlationId;
        _amount = amount;
        _currentBalance = currentBalance;
        _success = success;
    }

    public String getCorrelationId() {
        return _correlationId;
    }
    public double getAmount() {
        return _amount;
    }
    public double getCurrentBalance() {
        return _currentBalance;
    }
    public Boolean getSuccess() {
        return _success;
    }    
    public void setCorrelationId(String correlationId){
        _correlationId = correlationId;
    }
    public void setAmount(double amount) {
        _amount = amount;
    }
    public void setCurrentBalance(double currentBalance) {
        _currentBalance = currentBalance;
    }    
    public void setSuccess(Boolean success) {
        _success = success;
    }
}