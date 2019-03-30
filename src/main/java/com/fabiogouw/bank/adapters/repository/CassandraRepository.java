package com.fabiogouw.bank.adapters.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.*;
import com.fabiogouw.bank.domain.ports.AccountRepository;
import com.fabiogouw.bank.domain.Account;
import com.fabiogouw.bank.domain.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class CassandraRepository implements AccountRepository {

    private Cluster _cluster;
    private Session _session;
    private static final Logger _log = LoggerFactory.getLogger(CassandraRepository.class);

    @Value("${repository.cassandra.contactPoint}")
    private String _contactPoint;
    @Value("${repository.cassandra.keyspace}")
    private String _keyspace;
    @Value("${repository.cassandra.username}")
    private String _username;
    @Value("${repository.cassandra.password}")
    private String _password;

    private void connect() {
        try {
            if(_cluster == null || _cluster.isClosed()) {
                Builder b = Cluster.builder().addContactPoint(_contactPoint)
                        .withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM))
                        .withCredentials(_username, _password);
                _cluster = b.build();
            }
            if(_session == null || _session.isClosed()) {
                _session = _cluster.connect(_keyspace);
            }
        }
        catch(Exception ex) {
            _log.error("Failed to connect on Cassandra: {}", ex.getMessage());
        }
    }

    private CompletableFuture<Void> insert(String accountId, Date entryDatetime, UUID entryId, BigDecimal lastBalance, BigDecimal amount, UUID correlationId, String description, int entryType) {
        Batch batch = QueryBuilder.batch();
        Insert insert = QueryBuilder.insertInto("account_entries");
        insert.values(new String[]{
            "account_id", "entry_datetime", "entry_id", "amount", "correlation_id", "description", "entry_type"
        }, new Object[]{
            accountId, entryDatetime.getTime(), entryId, amount, correlationId, description, entryType
        });

        Update update = QueryBuilder.update("account_entries");
        update.where(QueryBuilder.eq("account_id", accountId))
                .onlyIf(QueryBuilder.eq("current_balance", lastBalance))
                .with(QueryBuilder.set("current_balance", lastBalance.add(amount)));

        batch.add(insert);
        batch.add(update);
        connect();
        return CompletableFuture.runAsync(() -> {
            if(!_session.isClosed()) {
                _log.debug(batch.toString());
                _session.execute(batch);
            }
        });   
    }

    private CompletableFuture<List<Transaction>> createNewAccount(String accountId) {
        Insert command = QueryBuilder.insertInto("account_entries").ifNotExists();
        command.values(new String[]{ "account_id", "current_balance" },
                new Object[]{ accountId, 0});
        connect();
        return CompletableFuture.supplyAsync(() -> {
            if(!_session.isClosed()) {
                _log.debug(command.toString());
                _session.execute(command);
            }
            return new ArrayList<>();
        });
    }

    private CompletableFuture<List<Transaction>> getBalance(String accountId, int maxItens) {
        return CompletableFuture.supplyAsync(() -> {
            connect();
            Select command = QueryBuilder.select(
                    "account_id",
                    "entry_datetime",
                    "entry_id",
                    "current_balance",
                    "amount",
                    "correlation_id",
                    "description",
                    "entry_type"
            ).from("account_entries");
            command.where(QueryBuilder.eq("account_id", accountId));
            command.orderBy(QueryBuilder.desc("entry_datetime"));
            command.limit(maxItens);
            _log.debug(command.toString());
            ResultSet rs = _session.execute(command);
            List<Row> rows = rs.all();
            List<Transaction> transactions = new ArrayList<>();
            if(!rows.isEmpty()) {
                for(Row r:rows) {
                    Transaction.EntryType type = Transaction.EntryType.from(r.getInt("entry_type"));
                    transactions.add(new Transaction(r.getString("account_id"),
                            r.getTimestamp("entry_datetime"),
                            r.getUUID("entry_id"),
                            r.getDecimal("current_balance"),
                            r.getDecimal("amount"),
                            r.getUUID("correlation_id"),
                            r.getString("description"), type));
                }
            }
            return transactions;
        });
    }

    @Override
    public CompletableFuture<Account> getAccount(String accountId) {
        CompletableFuture<List<Transaction>> future = getBalance(accountId, 5);
        return future
                .thenApply(transactions -> {
                    if(transactions.isEmpty()) {
                        return createNewAccount(accountId).join();
                    }
                    return transactions;
                })
                .thenApply(transactions -> new Account(accountId, transactions));
    }

    @Override
    public CompletableFuture<Account> saveAccount(Account account) {
        Transaction lastTransaction = account.getLastTransaction();
        if(lastTransaction != null) {
            CompletableFuture<Void> future = insert(account.getId(), lastTransaction.getEntryDatetime(), lastTransaction.getEntryId(), lastTransaction.getLastBalance(), lastTransaction.getAmount(), lastTransaction.getCorrelationId(), lastTransaction.getDescription(), lastTransaction.getEntryType().getValue());       
            return future.thenApply(action -> account);
        }
        else {
            return CompletableFuture.supplyAsync(() -> account);
        }
	}
}