package com.fabiogouw.bank.core.domain;

import java.util.List;

public class Account {
    private final String _id;
    private double _balance;
    private List<Object> _transactions;

    public Account(String id, double balance) {
        _id = id;
        _balance = balance;
    }

    public void AddTransaction(Transaction transaction) {

    }
}