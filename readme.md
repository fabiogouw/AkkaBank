# Akka Bank

This is a study of how to use Akka actors and exposing them as REST endpoints. It consists of a bank with several accounts that exposes operations like get balance and deposit / withdraw money. Some accounts can get loans from special loan accounts so communication between actors can be tried.
Akka Cluster is used to form a cluster of actors that represent checking account. Akka Cluster make shards of these accounts within the cluster.

To run, use the command below, replacing the command line arguments (or configuring environment variables).
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--repository.cassandra.contactPoint=172.17.0.3,--repository.cassandra.keyspace=ledger,--repository.cassandra.username=xxx,--repository.cassandra.password=xxx
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
  current_balance float static,
  amount float,
  correlation_id uuid,
  description text,
  entry_type int,
  PRIMARY KEY (account_id, entry_datetime, entry_id)
)
WITH CLUSTERING ORDER BY (entry_datetime DESC, entry_id ASC);
```
For consistency, it uses CAS (compare and set) considering the static column "current_balance".

While running, we can use curl to simulate some operations:

Get current balance
```
curl http://localhost:8080/accounts/1/balance
```

Make a deposit
```
curl --header "Content-Type: application/json" \
  --request POST \
  --data '{ "correlationId": "42e4a178-1d85-4e55-a6f0-d1c54b188eb6", "amount": 19.07  }' \
  http://localhost:8080/accounts/1/deposit
```

or a withdraw
```
curl --header "Content-Type: application/json" \
  --request POST \
  --data '{ "correlationId": "b2e55b88-dc48-49b0-b48b-82838e57fcf0", "amount": 2.98  }' \
  http://localhost:8080/accounts/1/withdraw
```