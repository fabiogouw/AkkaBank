package com.fabiogouw.bank.adapters.actors.messages;

public class DepositRequest extends OperationRequest {
    private static final long serialVersionUID = 3515932482649506598L;

    public DepositRequest(String accountId, String correlationId, double amount) {
        super(accountId, correlationId, amount);
    }
}
