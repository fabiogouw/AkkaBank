package com.galore.bank;

public class Operation {
    private String _correlationId;
    private String _accountId;
    private double _amount;

    public String getCorrelationId() {
        return _correlationId;
    }
    public String getAccountId() {
        return _accountId;
    }
    public double getAmount() {
        return _amount;
    }
    public void setCorrelationId(String correlationId){
        _correlationId = correlationId;
    }
    public void setAccountId(String accountId) {
        _accountId = accountId;
    }
    public void setAmount(double amount) {
        _amount = amount;
    }
}