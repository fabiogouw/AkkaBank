package com.fabiogouw.bank.adapters.actors.messages;

import java.io.Serializable;

public class AccountMessage implements Serializable {

    private static final long serialVersionUID = 4766278085642796988L;
    private final String _accountId;
    
    public AccountMessage(String accountId) {
        _accountId = accountId;
    }

    public String getAccountId() {
        return _accountId;
    }
}