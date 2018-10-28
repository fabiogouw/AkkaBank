package com.fabiogouw.bank.core.contracts;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.fabiogouw.bank.core.domain.Account;

public interface Ledger {
    public CompletableFuture<Double> getBalance(String accountId);
    public CompletableFuture<Void> insert(String accountId, Date entryDatetime, UUID entryId, double amount, UUID correlationId, String description, int entryType);
    public CompletableFuture<Void> saveBalance(String accountId, Date snapshotDate, double balance);
}