package com.galore.bank;

import java.util.Date;
import java.util.UUID;

import com.datastax.driver.core.Cluster;
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
            close();
        }
    }

    private void connect() {
        Builder b = Cluster.builder().addContactPoint(_contactPoint)
            .withCredentials(_username, _password);
        _cluster = b.build();
        _session = _cluster.connect(_keyspace);
    }

    private void close() {
        _session.close();
        _cluster.close();
    }
}