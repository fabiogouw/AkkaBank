package com.fabiogouw.bank.domain.ports;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Ledger {
    CompletableFuture<Double> getBalance(String accountId);
    CompletableFuture<Void> insert(String accountId, Date entryDatetime, UUID entryId, BigDecimal amount, UUID correlationId, String description, int entryType);
    CompletableFuture<Void> saveBalance(String accountId, Date snapshotDate, BigDecimal balance);
}