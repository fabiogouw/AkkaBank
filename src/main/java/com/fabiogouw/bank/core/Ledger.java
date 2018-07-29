package com.fabiogouw.bank.core;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Ledger {
    public enum EntryType {
        DEPOSIT(1),
        WITHDRAW(2);

        private final int _value;
        private EntryType(int value) {
            _value = value;
        }

        public int getValue() {
            return _value;
        }

        public static EntryType from(int value) {
            switch(value) {
                case 1:
                    return EntryType.DEPOSIT;
                default:
                    return EntryType.WITHDRAW;
            }
        }
    }
    public CompletableFuture<Double> getBalance(String accountId);
    public CompletableFuture<Void> insert(String accountId, Date entryDatetime, UUID entryId, double amount, UUID correlationId, String description, int entryType);
    public CompletableFuture<Void> saveBalance(String accountId, Date snapshotDate, double balance);
}