package com.fabiogouw.bank.adapters.repository;

import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.fabiogouw.bank.core.contracts.Ledger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FakeLedger implements Ledger {

    private static final Logger _log = LoggerFactory.getLogger(CassandraLedger.class);

    public CompletableFuture<Void> insert(String accountId, Date entryDatetime, UUID entryId, double amount, UUID correlationId, String description, int entryType) {           
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            _log.info("Inserting new transaction for '{}'...", accountId);
        });   
        return future;
    }

    public CompletableFuture<Void> saveBalance(String accountId, Date snapshotDate, double balance) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            _log.info("Saving balance for '{}'...", accountId);
        }); 
        return future;        
    }

    public CompletableFuture<Double> getBalance(String accountId) {
        CompletableFuture<Double> future = CompletableFuture.supplyAsync(() -> {
            _log.info("Getting random balance for '{}'...", accountId);
            Random rand = new Random();
            return rand.nextDouble() * 1000;
        }); 
        return future;
    }
}