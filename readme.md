# Akka Bank

This is a study of how to use Akka actors and exposing them as REST endpoints. It consists of a bank with several accounts that exposes operations like get balance and deposit / withdram money. Some accounts can get loans from special loan accounts so communication between actors can be tried.

It was developed using Visual Studio Code. To run, use the command below, replacing the command line arguments (or configuring environment variables).
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--cass.contactPoint=172.17.0.3,--cass.keyspace=ledger,--cass.username=xxx,--cass.password=xxx
```

To build, run the following command:
```bash
mvn package dockerfile:build
```

To create Cassandra's tables, use the script below
```cql
CREATE KEYSPACE ledger WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
CREATE TABLE ledger.account_entries (
  account_id text,
  entry_datetime timestamp,
  entry_id uuid,
  last_balance float,
  amount float,
  correlation_id uuid,
  description text,
  entry_type int,
  PRIMARY KEY (account_id, entry_datetime, entry_id)
)
WITH CLUSTERING ORDER BY (entry_datetime DESC, entry_id ASC);
```