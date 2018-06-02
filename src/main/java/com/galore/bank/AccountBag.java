package com.galore.bank;

import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class AccountBag {
    private final Map<String, ActorRef> _references = new HashMap<String, ActorRef>();

    public void add(String id, ActorRef accountRef) {
        _references.put(id, accountRef);
    }

    public ActorRef get(String id) {
        if(_references.containsKey(id)) {
            return (ActorRef) _references.get(id);
        }
        else {
            return null;
        }
    }
}