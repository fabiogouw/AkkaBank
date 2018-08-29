package com.fabiogouw.bank.repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.fabiogouw.bank.core.contracts.Ledger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.javatuples.Pair;
import org.slf4j.Logger;
import java.util.concurrent.*;

import com.datastax.driver.core.Session;

@Component
public class CassandraLedger implements Ledger {

    private Cluster _cluster;
    private Session _session;
    private static final Logger _log = LoggerFactory.getLogger(CassandraLedger.class);

    @Value("${cass.contactPoint}")
    private String _contactPoint;
    @Value("${cass.keyspace}")
    private String _keyspace;
    @Value("${cass.username}")
    private String _username;
    @Value("${cass.password}")
    private String _password;

    public CompletableFuture<Void> insert(String accountId, Date entryDatetime, UUID entryId, double amount, UUID correlationId, String description, int entryType) {
        Insert command = QueryBuilder.insertInto("account_entries");
        command.values(new String[]{
            "account_id", "entry_datetime", "entry_id", "amount", "correlation_id", "description", "entry_type"
        }, new Object[]{
            accountId, entryDatetime.getTime(), entryId, amount, correlationId, description, entryType
        });
        connect();
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                _log.info(command.toString());
                _session.execute(command);
            });   
            return future;         
        }
        finally {
            //close();
        }
    }

    public CompletableFuture<Void> saveBalance(String accountId, Date snapshotDate, double balance) {
        connect();
        Update command = QueryBuilder.update("balance_snapshots");
        command.with(QueryBuilder.set(balance, balance));
        command.where(QueryBuilder.eq("account_id", accountId))
            .and(QueryBuilder.eq("snapshot_date", snapshotDate.getTime()));
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            _log.info(command.toString());
            _session.execute(command);
        }); 
        return future;        
    }

    public CompletableFuture<Double> getBalance(String accountId) {
        CompletableFuture<Pair<Date, Double>> future = CompletableFuture.supplyAsync(() -> {
            double balance = 0;
            Date snapshotDate = new Date();            
            connect();
            Select command = QueryBuilder.select(new String[]{
                "balance", "snapshot_date"
            }).from("balance_snapshots");
            command.where(QueryBuilder.eq("account_id", accountId));
            _log.info(command.toString());
            ResultSet rs = _session.execute(command);
            Row balanceSnapshotRow = rs.one();
            if(balanceSnapshotRow != null) {
                balance = balanceSnapshotRow.getFloat("balance");
                snapshotDate = balanceSnapshotRow.getTimestamp("snapshot_date");
            }
            else {
                balance = 1000; // for simulations
            }
            return new Pair<Date, Double>(snapshotDate, balance);
        });
        return future.thenApply((balanceInfo) -> {
            connect();
            double balance = balanceInfo.getValue1();

            Select command = QueryBuilder.select(new String[]{
                "amount", "entry_type"
            }).from("account_entries");
            command.where(QueryBuilder.eq("account_id", accountId))
                .and(QueryBuilder.gt("entry_datetime", balanceInfo.getValue0().getTime()));
            command.orderBy(QueryBuilder.desc("entry_datetime"));
            _log.info(command.toString());
            ResultSet rs = _session.execute(command);
            List<Row> rows = rs.all();
            if(rows.size() > 0) {
                _log.info("Restoring balance for {}", accountId);
                for(Row r:rows) {
                    EntryType type = EntryType.from(r.getInt("entry_type"));
                    double amount = r.getFloat("amount");
                    if(type == EntryType.DEPOSIT) {
                        balance += amount;
                    }
                    if(type == EntryType.WITHDRAW) {
                        balance -= amount;
                    } 
                }
            }
            return balance;
        });
    }

    private void connect() {
        if(_cluster == null || _cluster.isClosed()) {
            Builder b = Cluster.builder().addContactPoint(_contactPoint)
                .withCredentials(_username, _password);
            _cluster = b.build();
        }
        if(_session == null || _session.isClosed()) {
            _session = _cluster.connect(_keyspace);
        }
    }

    private void close() {
        if(_session != null && !_session.isClosed()) {
            _session.close();
        }
        if(_cluster != null && !_cluster.isClosed()) {
            _cluster.close();
        }
    }
}