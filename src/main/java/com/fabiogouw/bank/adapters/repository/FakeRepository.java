package com.fabiogouw.bank.adapters.repository;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import com.fabiogouw.bank.core.contracts.AccountRepository;
import com.fabiogouw.bank.core.domain.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public class FakeRepository implements AccountRepository {

    private static final Logger _log = LoggerFactory.getLogger(FakeRepository.class);

    @Override
    public CompletableFuture<Account> getAccount(String accountId) {
        _log.info("Getting random balance for '{}'...", accountId);
        Random rand = new Random();
        return CompletableFuture.supplyAsync(() -> new Account(accountId, BigDecimal.valueOf(rand.nextDouble() * 1000)));
    }

    @Override
    public CompletableFuture<Account> saveAccount(Account account) {
        _log.info("Pretending to save an account entry '{}'...", account);
        return CompletableFuture.supplyAsync(() -> account);
    }
}