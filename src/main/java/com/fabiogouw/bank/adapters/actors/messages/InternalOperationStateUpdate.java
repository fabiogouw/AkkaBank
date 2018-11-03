package com.fabiogouw.bank.adapters.actors.messages;

import com.fabiogouw.bank.core.domain.Account;

import akka.actor.ActorRef;

public class InternalOperationStateUpdate {
    private final Account _account;
    private final ActorRef _respondTo;

    public InternalOperationStateUpdate(Account account, ActorRef respondTo) {
        _account = account;
        _respondTo = respondTo;
    }
   
    public Account getAccount() {
        return _account;
    }
    public ActorRef getRespondTo() {
        return _respondTo;
    }
}