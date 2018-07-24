package com.fabiogouw.bank;

public class OperationRequest {
    private String _correlationId;
    private double _amount;

    public String getCorrelationId() {
        return _correlationId;
    }
    public double getAmount() {
        return _amount;
    }
    public void setCorrelationId(String correlationId){
        _correlationId = correlationId;
    }
    public void setAmount(double amount) {
        _amount = amount;
    }
}