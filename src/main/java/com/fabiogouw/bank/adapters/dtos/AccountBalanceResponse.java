package com.fabiogouw.bank.adapters.dtos;

import java.math.BigDecimal;

public class AccountBalanceResponse {
    private String _accountId;
    private BigDecimal _balance;

    public AccountBalanceResponse() {

    }

    public AccountBalanceResponse(String accountId, BigDecimal balance) {
        _accountId = accountId;
        _balance = balance;
    }

    public String getAccountId() {
        return _accountId;
    }
    public BigDecimal getBalance() {
        return _balance;
    }
    public void setAccountId(String accountId){
        _accountId = accountId;
    }
    public void setBalance(BigDecimal amount) {
        _balance = amount;
    }
}