package com.fabiogouw.bank.adapters.actors.messages;

public class OperationRequest extends AccountMessage {
    private static final long serialVersionUID = -188612147356070992L;
    private final String _correlationId;
    private final double _amount;
    
    public OperationRequest(String accountId, String correlationId, double amount) {
        super(accountId);
        _correlationId = correlationId;
        _amount = amount;
    }
    public String getCorrelationId() {
        return _correlationId;
    }
    public double getAmount() {
        return _amount;
    }
}