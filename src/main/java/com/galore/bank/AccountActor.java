package com.galore.bank;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class AccountActor extends AbstractActor {

    static class BalanceRequest {

    }

    static class BalanceResponse {
        private String _id;
        private double _balance;
        
        public BalanceResponse(String id, double balance) {
            _id = id;
            _balance = balance;
        }
        public String getId() {
            return _id;
        }
        public double getBalance() {
            return _balance;
        }
    }

    static Props props(String id, double initialBalance) {
        return Props.create(AccountActor.class, id, initialBalance);
    }

    private String _id;
    private double _balance;

    public AccountActor(String id, double initialBalance) {
        _id = id;
        _balance = initialBalance;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(BalanceRequest.class, req -> {
                getSender().tell(new BalanceResponse(_id, _balance), getSelf());
            })
            .build();
    }
}