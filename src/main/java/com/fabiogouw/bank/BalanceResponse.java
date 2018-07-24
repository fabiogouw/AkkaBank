package com.fabiogouw.bank;

public class BalanceResponse {
    private String _accountId;
    private double _balance;

    public BalanceResponse() {

    }

    public BalanceResponse(String accountId, double balance) {
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