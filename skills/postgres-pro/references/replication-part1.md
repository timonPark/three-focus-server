<!-- part 1/3 of replication.md -->

# PostgreSQL Replication

## Streaming Replication (Physical)

### Primary Server Setup

```sql
-- postgresql.conf
wal_level = replica
max_wal_senders = 10
max_replication_slots = 10
wal_keep_size = 1GB  # Or 1024MB for older versions
hot_standby = on
archive_mode = on
archive_command = 'cp %p /var/lib/postgresql/wal_archive/%f'

-- pg_hba.conf (allow replication connections)
host replication replicator 10.0.0.0/24 scram-sha-256
```

```sql
-- Create replication user
CREATE ROLE replicator WITH REPLICATION LOGIN PASSWORD 'secure_password';

-- Create replication slot (prevents WAL deletion)
SELECT * FROM pg_create_physical_replication_slot('replica_1');
```

### Standby Server Setup

```bash
# Stop PostgreSQL on standby
systemctl stop postgresql

# Remove data directory
rm -rf /var/lib/postgresql/14/main/*

# Base backup from primary
pg_basebackup -h primary-host -D /var/lib/postgresql/14/main \
  -U replicator -P -v -R -X stream -S replica_1

# -R creates standby.signal and recovery config
# -X stream: stream WAL during backup
# -S replica_1: use replication slot
```

```sql
-- standby.signal file created by pg_basebackup -R
-- recovery parameters in postgresql.auto.conf:
primary_conninfo = 'host=primary-host port=5432 user=replicator password=secure_password'
primary_slot_name = 'replica_1'
```

### Monitoring Replication

```sql
-- On primary: Check replication status
SELECT
  client_addr,
  state,
  sync_state,
  sent_lsn,
  write_lsn,
  flush_lsn,
  replay_lsn,
  pg_wal_lsn_diff(sent_lsn, replay_lsn) as lag_bytes
FROM pg_stat_replication;

-- On standby: Check replay lag
SELECT
  now() - pg_last_xact_replay_timestamp() AS replication_lag;

-- Check replication slots
SELECT
  slot_name,
  slot_type,
  active,
  restart_lsn,
  pg_wal_lsn_diff(pg_current_wal_lsn(), restart_lsn) as retained_bytes
FROM pg_replication_slots;
```

### Synchronous Replication

```sql
-- postgresql.conf on primary
synchronous_commit = on
synchronous_standby_names = 'FIRST 1 (replica_1, replica_2)'
# Waits for 1 standby to confirm before commit

# Options:
# FIRST n (names): Wait for n standbys
# ANY n (names): Wait for any n standbys
# name: Wait for specific standby

-- Query to check sync status
SELECT
  application_name,
  sync_state,
  state
FROM pg_stat_replication;
-- sync_state: sync (synchronous), async, potential
```

## Logical Replication (Row-level)

### Publisher Setup

```sql
-- postgresql.conf
wal_level = logical
max_replication_slots = 10
max_wal_senders = 10

-- Create publication (all tables)
CREATE PUBLICATION my_publication FOR ALL TABLES;

-- Or specific tables
CREATE PUBLICATION my_publication FOR TABLE users, orders;

-- Or tables matching pattern (PG15+)
CREATE PUBLICATION my_publication FOR TABLES IN SCHEMA public;

-- With row filters (PG15+)
CREATE PUBLICATION active_users FOR TABLE users WHERE (active = true);

-- View publications
SELECT * FROM pg_publication;
SELECT * FROM pg_publication_tables;
```

### Subscriber Setup

```sql
-- Create subscription (creates replication slot on publisher)
CREATE SUBSCRIPTION my_subscription
CONNECTION 'host=publisher-host port=5432 dbname=mydb user=replicator password=pass'
PUBLICATION my_publication;

-- Subscription options
CREATE SUBSCRIPTION my_subscription
CONNECTION 'host=publisher-host dbname=mydb user=replicator'
PUBLICATION my_publication
WITH (
  copy_data = true,           -- Initial data copy
  create_slot = true,          -- Create replication slot
  enabled = true,              -- Start immediately
  slot_name = 'my_sub_slot',
  synchronous_commit = 'off'   -- Performance vs durability
);

-- View subscriptions
SELECT * FROM pg_subscription;
SELECT * FROM pg_stat_subscription;

-- Manage subscription
ALTER SUBSCRIPTION my_subscription DISABLE;
ALTER SUBSCRIPTION my_subscription ENABLE;
ALTER SUBSCRIPTION my_subscription REFRESH PUBLICATION;
DROP SUBSCRIPTION my_subscription;
```

### Logical Replication Monitoring

```sql
-- On publisher: Check replication slots
SELECT
  slot_name,
  plugin,
  slot_type,
  active,
  pg_wal_lsn_diff(pg_current_wal_lsn(), confirmed_flush_lsn) as lag_bytes
FROM pg_replication_slots
WHERE slot_type = 'logical';

-- On subscriber: Check subscription status
SELECT
  subname,
  pid,
  received_lsn,
  latest_end_lsn,
  last_msg_send_time,
  last_msg_receipt_time,
  latest_end_time
FROM pg_stat_subscription;
```

