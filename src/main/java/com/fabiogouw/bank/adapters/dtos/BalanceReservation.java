package com.fabiogouw.bank.adapters.dtos;

import akka.actor.ActorRef;

import java.math.BigDecimal;

public class BalanceReservation {
    private String _correlationId;
    private BigDecimal _amount;
    private ActorRef _originalRequester;

    public BalanceReservation(String correlationId, BigDecimal amount, ActorRef originalRequester) {
        _correlationId = correlationId;
        _amount = amount;
        _originalRequester = originalRequester;
    }

    public String getCorrelationId() {
        return _correlationId;
    }
    public BigDecimal getAmount() {
        return _amount;
    }
    public ActorRef getOriginalRequester() {
        return _originalRequester;
    }
}