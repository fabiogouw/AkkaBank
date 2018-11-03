package com.fabiogouw.bank.adapters.dtos;

public class AccountBalanceResponse {
    private String _accountId;
    private double _balance;

    public AccountBalanceResponse() {

    }

    public AccountBalanceResponse(String accountId, double balance) {
        _accountId = accountId;
        _balance = balance;
    }

    public String getAccountId() {
        return _accountId;
    }
    public double getBalance() {
        return _balance;
    }
    public void setAccountId(String accountId){
        _accountId = accountId;
    }
    public void setBalance(double amount) {
        _balance = amount;
    }
}