package com.fabiogouw.bank.adapters.actors.messages;

import java.math.BigDecimal;

public class WithdrawRequest extends OperationRequest {
    private static final long serialVersionUID = 3523795952970405852L;

    public WithdrawRequest(String accountId, String correlationId, BigDecimal amount) {
        super(accountId, correlationId, amount);
    }
}