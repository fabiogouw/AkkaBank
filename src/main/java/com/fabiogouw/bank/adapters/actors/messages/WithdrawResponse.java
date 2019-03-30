package com.fabiogouw.bank.adapters.actors.messages;

import java.math.BigDecimal;

public class WithdrawResponse extends OperationResponse {
    private static final long serialVersionUID = 7175374830232354388L;

    public WithdrawResponse(String correlationId, BigDecimal currentBalance, Boolean success) {
        super(correlationId, currentBalance, success);
    }
}