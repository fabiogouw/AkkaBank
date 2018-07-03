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
CREATE TABLE ledger.account_entries (
  account_id text,
  entry_datetime timestamp,
  entry_id uuid,
  amount float,
  correlation_id uuid,
  description text,
  entry_type int,
  PRIMARY KEY (account_id, entry_datetime, entry_id)
)
WITH CLUSTERING ORDER BY (entry_datetime DESC, entry_id ASC)
 AND bloom_filter_fp_chance = 0.01
 AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
 AND comment = ''
 AND compaction = {'class': 'SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
 AND compression = {'chunk_length_in_kb': '64', 'class': 'LZ4Compressor'}
 AND crc_check_chance = 1.0
 AND dclocal_read_repair_chance = 0.1
 AND default_time_to_live = 0
 AND gc_grace_seconds = 864000
 AND max_index_interval = 2048
 AND memtable_flush_period_in_ms = 0
 AND min_index_interval = 128
 AND read_repair_chance = 0.0
 AND speculative_retry = '99PERCENTILE';

 CREATE TABLE ledger.balance_snapshots (
  account_id text PRIMARY KEY,
  balance float,
  snapshot_date timestamp
)
WITH bloom_filter_fp_chance = 0.01
 AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
 AND comment = ''
 AND compaction = {'class': 'SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
 AND compression = {'chunk_length_in_kb': '64', 'class': 'LZ4Compressor'}
 AND crc_check_chance = 1.0
 AND dclocal_read_repair_chance = 0.1
 AND default_time_to_live = 0
 AND gc_grace_seconds = 864000
 AND max_index_interval = 2048
 AND memtable_flush_period_in_ms = 0
 AND min_index_interval = 128
 AND read_repair_chance = 0.0
 AND speculative_retry = '99PERCENTILE';
```