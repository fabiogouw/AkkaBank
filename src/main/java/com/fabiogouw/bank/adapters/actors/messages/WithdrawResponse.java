package com.fabiogouw.bank.adapters.actors.messages;

public class WithdrawResponse extends OperationResponse {
    private static final long serialVersionUID = 7175374830232354388L;

    public WithdrawResponse(String correlationId, double currentBalance, Boolean success) {
        super(correlationId, currentBalance, success);
    }
}