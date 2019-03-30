package com.fabiogouw.bank.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Date;

import com.fabiogouw.bank.domain.Transaction.EntryType;

public class Account {
    private final String _id;
    private BigDecimal _balance = BigDecimal.valueOf(0l);;
    private final List<Transaction> _transactions = new ArrayList<>();
    private Transaction _lastTransaction;

    public Account(String id, BigDecimal initialBalance) {
        _id = id;
        _balance = initialBalance;
    }

    public Account(String id, List<Transaction> transactions) {
        _id = id;
        _transactions.clear();
        _transactions.addAll(transactions);
        if(!_transactions.isEmpty()) {
            Transaction lastTransaction = _transactions.get(0);
            _balance = lastTransaction.getLastBalance();
        }
    }    

    public String getId() {
        return _id;
    }

    public Boolean Withdraw(UUID correlationId, BigDecimal amount) {
        if(_balance.compareTo(amount)  >= 0) {
            _lastTransaction = new Transaction(_id, new Date(), UUID.randomUUID(), _balance,  amount.negate(), correlationId, "withdraw", EntryType.WITHDRAW);
            _transactions.add(_lastTransaction);
            _balance = _balance.add(_lastTransaction.getAmount());
            return true;
        }
        return false;
    }

    public Boolean Deposit(UUID correlationId, BigDecimal amount) {
        _lastTransaction = new Transaction(_id, new Date(), UUID.randomUUID(), _balance, amount, correlationId, "deposit", EntryType.DEPOSIT);
        _transactions.add(_lastTransaction);
        _balance = _balance.add(_lastTransaction.getAmount());
        return true;
    }

    public Transaction getLastTransaction() {
        return _lastTransaction;
    }

    public BigDecimal getBalance() {
        return _balance;
    }
}