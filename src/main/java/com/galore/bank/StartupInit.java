package com.galore.bank;

import java.util.Random;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@Component
public class StartupInit {
    private final AccountBag _accountBag;
    private final ActorSystem _system;

    public StartupInit(AccountBag accountBag, ActorSystem system) {
        _accountBag = accountBag;
        _system = system;
    }

    @PostConstruct
    public void init() {
        Random rnd = new Random();
        for (int i = 1; i <= 1000; i++) {
            String id = String.valueOf(i);
            ActorRef accountRef = _system.actorOf(AccountActor.props(id, 1000 * rnd.nextDouble()), id);
            _accountBag.add(id , accountRef);
        }
    }
}