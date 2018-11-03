package com.fabiogouw.bank.adapters.actors.messages;

public class DepositResponse extends OperationResponse {
    private static final long serialVersionUID = -5136902613153736547L;

    public DepositResponse(String correlationId, double currentBalance, Boolean success) {
        super(correlationId, currentBalance, success);
    }
}