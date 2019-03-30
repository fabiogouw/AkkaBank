package com.fabiogouw.bank.adapters.actors.messages;

import java.math.BigDecimal;

public class DepositRequest extends OperationRequest {
    private static final long serialVersionUID = 3515932482649506598L;

    public DepositRequest(String accountId, String correlationId, BigDecimal amount) {
        super(accountId, correlationId, amount);
    }
}
