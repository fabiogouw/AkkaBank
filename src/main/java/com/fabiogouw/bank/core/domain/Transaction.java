package com.fabiogouw.bank.core.domain;

import java.util.Date;
import java.util.UUID;

public class Transaction {

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

    private String _accountId;
    private Date _entryDatetime;
    private UUID _entryId;
    private double _amount;
    private UUID _correlationId;
    private String _description;
    private EntryType _entryType;

    public Transaction(Date entryDatetime, UUID entryId, double amount, UUID correlationId, String description, EntryType entryType) {
        _entryDatetime = entryDatetime;
        _entryId = entryId;
        _amount = amount;
        _correlationId = correlationId;
        _description = description;
        _entryType = entryType;
    }

    public String getAccountId() {
        return _accountId;
    }

    public Date getEntryDatetime() {
        return _entryDatetime;
    }

    public UUID getEntryId() {
        return _entryId;
    }

    public double getAmount() {
        return _amount;
    }

    public UUID getCorrelationId() {
        return _correlationId;
    }

    public String getDescription() {
        return _description;
    }

    public EntryType getEntryType() {
        return _entryType;
    }
}