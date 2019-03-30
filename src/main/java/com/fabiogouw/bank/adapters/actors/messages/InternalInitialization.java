package com.fabiogouw.bank.adapters.actors.messages;

import com.fabiogouw.bank.domain.Account;

public class InternalInitialization {
    private final Account _account;

    public InternalInitialization(Account account) {
        _account = account;
    }
   
    public Account getAccount() {
        return _account;
    }
}  