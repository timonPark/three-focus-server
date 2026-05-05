<!-- part 3/3 of replication.md -->

## Backup and Point-in-Time Recovery (PITR)

### WAL Archiving Setup

```sql
-- postgresql.conf
wal_level = replica
archive_mode = on
archive_command = 'test ! -f /backup/wal/%f && cp %p /backup/wal/%f'
archive_timeout = 300  # Force archive every 5 minutes

-- Or use pg_archivecleanup
archive_command = 'pgbackrest --stanza=main archive-push %p'
```

### Base Backup with pg_basebackup

```bash
# Full backup
pg_basebackup -h localhost -U postgres \
  -D /backup/base/$(date +%Y%m%d) \
  -Ft -z -P -X fetch

# -Ft: tar format
# -z: gzip compression
# -P: progress
# -X fetch: include WAL files
```

### Point-in-Time Recovery

```bash
# Stop PostgreSQL
systemctl stop postgresql

# Restore base backup
rm -rf /var/lib/postgresql/14/main/*
tar -xzf /backup/base/20241201/base.tar.gz -C /var/lib/postgresql/14/main

# Create recovery.signal
touch /var/lib/postgresql/14/main/recovery.signal

# Configure recovery
# postgresql.conf or postgresql.auto.conf:
restore_command = 'cp /backup/wal/%f %p'
recovery_target_time = '2024-12-01 14:30:00'
# Or: recovery_target_xid, recovery_target_name, recovery_target_lsn

# Start PostgreSQL (will recover to target)
systemctl start postgresql

# After recovery, check
SELECT pg_is_in_recovery();  # Should be false after recovery completes
```

## Monitoring Best Practices

```sql
-- Create monitoring view
CREATE VIEW replication_status AS
SELECT
  client_addr,
  application_name,
  state,
  sync_state,
  pg_wal_lsn_diff(pg_current_wal_lsn(), replay_lsn) / 1024 / 1024 AS lag_mb,
  (pg_wal_lsn_diff(pg_current_wal_lsn(), replay_lsn)::float /
   (1024 * 1024 * 16))::int AS estimated_wal_segments_behind
FROM pg_stat_replication;

-- Alert if lag > 100MB
SELECT * FROM replication_status WHERE lag_mb > 100;

-- Check replication slot disk usage
SELECT
  slot_name,
  pg_size_pretty(
    pg_wal_lsn_diff(pg_current_wal_lsn(), restart_lsn)
  ) as retained_wal
FROM pg_replication_slots;
```

## Troubleshooting

```sql
-- Replication broken?
-- 1. Check pg_stat_replication on primary
SELECT * FROM pg_stat_replication;

-- 2. Check logs on standby
-- tail -f /var/log/postgresql/postgresql-14-main.log

-- 3. Check replication slot exists
SELECT * FROM pg_replication_slots WHERE slot_name = 'replica_1';

-- 4. Recreate slot if missing
SELECT pg_create_physical_replication_slot('replica_1');

-- 5. Check WAL files available
-- ls -lh /var/lib/postgresql/14/main/pg_wal/

-- Standby too far behind?
-- Option 1: Increase wal_keep_size
-- Option 2: Use replication slots
-- Option 3: Re-run pg_basebackup
```
