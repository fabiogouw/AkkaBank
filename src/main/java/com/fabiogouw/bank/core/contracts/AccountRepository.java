package com.fabiogouw.bank.core.contracts;

import java.util.concurrent.CompletableFuture;

import com.fabiogouw.bank.core.domain.Account;

public interface AccountRepository {
    public CompletableFuture<Account> getAccount(String accountId);
    public CompletableFuture<Account> saveAccount(Account account);
}