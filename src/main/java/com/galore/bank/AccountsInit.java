package com.galore.bank;

import java.util.Random;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@Component
public class AccountsInit {
    private final AccountBag _accountBag;
    private final ActorSystem _system;
    private final Ledger _ledger;

    public AccountsInit(AccountBag accountBag, ActorSystem system, Ledger ledger) {
        _accountBag = accountBag;
        _system = system;
        _ledger = ledger;
    }

    @PostConstruct
    public void init() {
        for (int i = 1; i <= 1000; i++) {
            String id = String.valueOf(i);
            ActorRef accountRef = _system.actorOf(AccountActor.props(id, 0, _ledger), id);
            _accountBag.add(id , accountRef);
        }
    }
}