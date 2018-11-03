package com.fabiogouw.bank.adapters.actors.messages;

import java.io.Serializable;

public class BalanceResponse implements Serializable {
    private static final long serialVersionUID = 1302757287444314441L;
    private final double _balance;

    public BalanceResponse(double balance) {
        _balance = balance;
    }
    public double getBalance() {
        return _balance;
    }
}