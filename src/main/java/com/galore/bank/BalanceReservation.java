package com.galore.bank;

import akka.actor.ActorRef;

public class BalanceReservation {
    private String _correlationId;
    private double _amount;
    private ActorRef _originalRequester;

    public BalanceReservation(String correlationId, double amount, ActorRef originalRequester) {
        _correlationId = correlationId;
        _amount = amount;
        _originalRequester = originalRequester;
    }

    public String getCorrelationId() {
        return _correlationId;
    }
    public double getAmount() {
        return _amount;
    }
    public ActorRef getOriginalRequester() {
        return _originalRequester;
    }
}