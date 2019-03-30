package com.fabiogouw.bank.domain.ports;

import java.util.concurrent.CompletableFuture;

import com.fabiogouw.bank.domain.Account;

public interface AccountRepository {
    CompletableFuture<Account> getAccount(String accountId);
    CompletableFuture<Account> saveAccount(Account account);
}