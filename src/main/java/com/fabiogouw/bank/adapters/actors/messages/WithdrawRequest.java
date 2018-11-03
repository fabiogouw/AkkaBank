package com.fabiogouw.bank.adapters.actors.messages;

public class WithdrawRequest extends OperationRequest {
    private static final long serialVersionUID = 3523795952970405852L;

    public WithdrawRequest(String accountId, String correlationId, double amount) {
        super(accountId, correlationId, amount);
    }
}