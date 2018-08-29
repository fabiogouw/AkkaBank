package com.fabiogouw.bank.core.domain;

import java.util.Date;
import java.util.UUID;

public class Transaction {
    private String _accountId;
    private Date _entryDatetime;
    private UUID _entryId;
    private double _amount;
    private UUID _correlationId;
    private String _description;
    private int _entryType;

    public Transaction(String accountId, Date entryDatetime, UUID entryId, double amount, UUID correlationId, String description, int entryType) {
        _accountId = accountId;
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

    public int getEntryType() {
        return _entryType;
    }
}