<!-- part 2/3 of replication.md -->

## Cascading Replication

```
Primary -> Standby1 -> Standby2
```

```sql
-- On Standby1 (acts as relay)
-- postgresql.conf
hot_standby = on
max_wal_senders = 10
wal_keep_size = 1GB

-- Standby2 connects to Standby1
-- Same setup as regular standby, but primary_conninfo points to Standby1
primary_conninfo = 'host=standby1-host user=replicator...'
```

## Delayed Replication (Delayed Standby)

```sql
-- On standby: postgresql.conf
recovery_min_apply_delay = '4h'

-- Useful for:
-- - Protection against accidental data deletion
-- - Rolling back to specific point in time
-- - Can promote delayed standby to recover dropped table

-- Check delay
SELECT now() - pg_last_xact_replay_timestamp() AS current_delay;
```

## Failover and Promotion

### Manual Failover

```bash
# On standby server
# Promote standby to primary
pg_ctl promote -D /var/lib/postgresql/14/main

# Or use SQL
SELECT pg_promote();

# Verify promotion
SELECT pg_is_in_recovery();  -- Should return false
```

### Automatic Failover with pg_auto_failover

```bash
# Install pg_auto_failover
apt-get install pg-auto-failover

# Setup monitor node
pg_autoctl create monitor --hostname monitor-host --pgdata /var/lib/monitor

# Setup primary
pg_autoctl create postgres \
  --hostname primary-host \
  --pgdata /var/lib/postgresql/14/main \
  --monitor postgres://monitor-host/pg_auto_failover

# Setup standby
pg_autoctl create postgres \
  --hostname standby-host \
  --pgdata /var/lib/postgresql/14/main \
  --monitor postgres://monitor-host/pg_auto_failover

# Check status
pg_autoctl show state
```

### Patroni (Production HA Solution)

```yaml
# patroni.yml
scope: postgres-cluster
name: node1

restapi:
  listen: 0.0.0.0:8008
  connect_address: node1:8008

etcd:
  hosts: etcd1:2379,etcd2:2379,etcd3:2379

bootstrap:
  dcs:
    ttl: 30
    loop_wait: 10
    retry_timeout: 10
    maximum_lag_on_failover: 1048576
    postgresql:
      use_pg_rewind: true
      parameters:
        max_connections: 100
        max_wal_senders: 10
        wal_level: replica

postgresql:
  listen: 0.0.0.0:5432
  connect_address: node1:5432
  data_dir: /var/lib/postgresql/14/main
  authentication:
    replication:
      username: replicator
      password: repl_password
    superuser:
      username: postgres
      password: postgres_password
```

## Connection Pooling for HA

### PgBouncer Configuration

```ini
# pgbouncer.ini
[databases]
mydb = host=primary-host port=5432 dbname=mydb

[pgbouncer]
listen_addr = *
listen_port = 6432
auth_type = scram-sha-256
auth_file = /etc/pgbouncer/userlist.txt
pool_mode = transaction
max_client_conn = 1000
default_pool_size = 25
reserve_pool_size = 5
```

### HAProxy for Load Balancing

```
# haproxy.cfg
frontend postgres_frontend
    bind *:5432
    mode tcp
    default_backend postgres_backend

backend postgres_backend
    mode tcp
    option tcp-check
    tcp-check expect string is_master:true

    server primary primary-host:5432 check
    server standby1 standby1-host:5432 check backup
    server standby2 standby2-host:5432 check backup
```

