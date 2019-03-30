package com.fabiogouw.bank.adapters.actors.messages;

import java.io.Serializable;
import java.math.BigDecimal;

public class BalanceResponse implements Serializable {
    private static final long serialVersionUID = 1302757287444314441L;
    private final BigDecimal _balance;

    public BalanceResponse(BigDecimal balance) {
        _balance = balance;
    }
    public BigDecimal getBalance() {
        return _balance;
    }
}