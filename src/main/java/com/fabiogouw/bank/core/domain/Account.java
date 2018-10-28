package com.fabiogouw.bank.core.domain;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Date;

import com.fabiogouw.bank.core.domain.Transaction.EntryType;

public class Account {
    private final String _id;
    private double _balance;
    private List<Transaction> _transactions = new ArrayList<>();
    private Transaction _lastTransaction;

    public Account(String id, double initialBalance) {
        _id = id;
        _balance = initialBalance;
    }

    public String getId() {
        return _id;
    }

    public Boolean Withdraw(UUID correlationId, double amount) {
        if(_balance >= amount) {
            _lastTransaction = new Transaction(new Date(), UUID.randomUUID(), -1 * amount, correlationId, "withdraw", EntryType.WITHDRAW);
            _transactions.add(_lastTransaction);
            _balance -= _lastTransaction.getAmount();
            return true;
        }
        return false;
    }

    public Boolean Deposit(UUID correlationId, double amount) {
        _lastTransaction = new Transaction(new Date(), UUID.randomUUID(), amount, correlationId, "deposit", EntryType.DEPOSIT);
        _transactions.add(_lastTransaction);
        _balance += _lastTransaction.getAmount();
        return true;
    }

    public Transaction getLastTransaction() {
        return _lastTransaction;
    }

    public double getBalance() {
        return _balance;
    }
}