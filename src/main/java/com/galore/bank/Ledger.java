package com.galore.bank;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Cluster.Builder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;

import com.datastax.driver.core.Session;

@Component
public class Ledger {

    public enum EntryType {
        DEPOSIT(1),
        WITHDRAW(2);

        private final int _value;
        private EntryType(int value) {
            _value = value;
        }

        public int getValue() {
            return _value;
        }

        public static EntryType from(int value) {
            switch(value) {
                case 1:
                    return EntryType.DEPOSIT;
                default:
                    return EntryType.WITHDRAW;
            }
        }
    }

    private Cluster _cluster;
    private Session _session;
    private static final Logger _log = LoggerFactory.getLogger(Ledger.class);

    @Value("${cass.contactPoint}")
    private String _contactPoint;
    @Value("${cass.keyspace}")
    private String _keyspace;
    @Value("${cass.username}")
    private String _username;
    @Value("${cass.password}")
    private String _password;

    public void insert(String accountId, Date entryDatetime, UUID entryId, double amount, UUID correlationId, String description, int entryType) {
        StringBuilder sb = new StringBuilder("INSERT INTO account_entries ");
        sb.append("(account_id, entry_datetime, entry_id, amount, correlation_id, description, entry_type) ")
            .append("VALUES (")
            .append("'").append(accountId).append("', ")
            .append(entryDatetime.getTime()).append(", ")
            .append(entryId).append(", ")
            .append(amount).append(", ")
            .append(correlationId).append(", ")
            .append("'").append(description).append("', ")
            .append(entryType)
            .append(");");
        connect();
        try {
            String command = sb.toString();
            _log.info(command);
            _session.execute(command);
        }
        finally {
            //close();
        }
    }

    public void saveBalance(String accountId, Date snapshotDate, double balance) {
        connect();
        StringBuilder sb = new StringBuilder("UPDATE balance_snapshots SET ");
        sb.append("balance =").append(balance).append(", ")
            .append("snapshot_date =").append(snapshotDate.getTime()).append(" ")
            .append("WHERE account_id ='").append(accountId).append("' ");
        String command = sb.toString();
        _log.info(command);
        _session.execute(command);
    }

    public double getBalance(String accountId) {
        double balance = 0;
        Date snapshotDate = new Date();
        connect();

        StringBuilder sb = new StringBuilder("SELECT balance, snapshot_date FROM balance_snapshots WHERE ")
            .append("account_id ='").append(accountId).append("' ");
        String command = sb.toString();
        _log.info(command);
        ResultSet rs = _session.execute(command);
        Row balanceSnapshotRow = rs.one();
        if(balanceSnapshotRow != null) {
            balance = balanceSnapshotRow.getDouble("balance");
            snapshotDate = balanceSnapshotRow.getTimestamp("snapshot_date");
        }
        sb = new StringBuilder("SELECT amount, entry_type FROM account_entries WHERE ");
        sb.append("account_id ='").append(accountId).append("' ")
            .append("AND entry_datetime <").append(snapshotDate.getTime()).append(" ")
            .append("ORDER BY entry_datetime DESC;");
        command = sb.toString();
        _log.info(command);
        rs = _session.execute(command);
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