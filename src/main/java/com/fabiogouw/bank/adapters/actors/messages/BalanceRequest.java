package com.fabiogouw.bank.adapters.actors.messages;

public class BalanceRequest extends AccountMessage {
    private static final long serialVersionUID = -2216452416044790679L;

    public BalanceRequest(String accountId) {
        super(accountId);
    }
}