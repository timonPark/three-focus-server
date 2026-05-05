<!-- part 2/2 of infrastructure-security.md -->


### CIS Benchmark Scanning

```bash
# Docker CIS benchmark
docker run --net host --pid host --cap-add audit_control \
  -v /var/lib:/var/lib -v /var/run/docker.sock:/var/run/docker.sock \
  docker/docker-bench-security

# Kubernetes CIS benchmark
kube-bench run --targets master,node

# Linux system hardening
lynis audit system --quick
```

### Compliance as Code (InSpec)

```ruby
# controls/baseline.rb
control 'ssh-hardening' do
  impact 1.0
  title 'SSH Security Configuration'

  describe sshd_config do
    its('Protocol') { should eq '2' }
    its('PermitRootLogin') { should eq 'no' }
    its('PasswordAuthentication') { should eq 'no' }
  end
end

control 'encryption-at-rest' do
  impact 1.0
  title 'S3 Encryption Enabled'

  describe aws_s3_bucket('my-bucket') do
    it { should have_default_encryption_enabled }
  end
end
```

## Secrets Management

### HashiCorp Vault

```bash
# Initialize and configure
vault operator init
vault secrets enable -path=secret kv-v2

# Store secrets
vault kv put secret/app/config api_key="secret123"

# Dynamic database credentials
vault secrets enable database
vault write database/config/postgresql \
  plugin_name=postgresql-database-plugin \
  allowed_roles="app" \
  connection_url="postgresql://{{username}}:{{password}}@localhost:5432/" \
  username="vault" password="vaultpass"

vault write database/roles/app \
  db_name=postgresql \
  creation_statements="CREATE ROLE \"{{name}}\" WITH LOGIN PASSWORD '{{password}}';" \
  default_ttl="1h" max_ttl="24h"
```

### Kubernetes Secrets with External Secrets Operator

```yaml
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: vault-backend
spec:
  provider:
    vault:
      server: "https://vault.example.com"
      path: "secret"
      auth:
        kubernetes:
          role: "app-role"
---
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: app-secrets
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: vault-backend
  target:
    name: app-secrets
  data:
  - secretKey: api_key
    remoteRef:
      key: secret/app/config
      property: api_key
```

## Security Monitoring

### SIEM Log Shipping (Filebeat)

```yaml
filebeat.inputs:
- type: log
  paths:
    - /var/log/auth.log
    - /var/log/nginx/*.log
  fields:
    environment: production

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "security-logs-%{+yyyy.MM.dd}"
```

## Quick Reference

| Area | Tool | Purpose |
|------|------|---------|
| Cloud Security | Prowler, ScoutSuite | AWS/Azure/GCP audit |
| Container | Trivy, Clair | Image scanning |
| IaC | Checkov, tfsec | Terraform/CloudFormation |
| Secrets | Vault, Sealed Secrets | Secret management |
| Compliance | InSpec, OpenSCAP | CIS benchmarks |
| Monitoring | ELK, Splunk | SIEM |

| Framework | Focus | Key Controls |
|-----------|-------|--------------|
| SOC 2 | Security controls | Access, encryption, monitoring |
| ISO 27001 | ISMS | Policy, risk, audit |
| PCI DSS | Payment security | Network segmentation, encryption |
| HIPAA | Healthcare | Encryption, access logs |
| GDPR | Data privacy | Consent, retention, DLP |
