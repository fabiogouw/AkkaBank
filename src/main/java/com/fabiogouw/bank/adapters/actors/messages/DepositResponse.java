package com.fabiogouw.bank.adapters.actors.messages;

import java.math.BigDecimal;

public class DepositResponse extends OperationResponse {
    private static final long serialVersionUID = -5136902613153736547L;

    public DepositResponse(String correlationId, BigDecimal currentBalance, Boolean success) {
        super(correlationId, currentBalance, success);
    }
}