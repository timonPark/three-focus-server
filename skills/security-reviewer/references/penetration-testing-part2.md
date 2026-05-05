<!-- part 2/2 of penetration-testing.md -->

## Cloud Security Testing

### AWS

```bash
# S3 bucket enumeration
aws s3 ls s3://bucket-name --no-sign-request
aws s3api get-bucket-acl --bucket bucket-name

# IAM enumeration
aws iam get-user
aws iam list-attached-user-policies --user-name username
```

### Container & Kubernetes

```bash
# Docker escape testing
docker inspect container_id | grep -i privileged
docker inspect container_id | grep -A 5 Mounts

# Kubernetes
kubectl get pods --all-namespaces
kubectl get secrets --all-namespaces
kubectl auth can-i --list
```

## Exploitation Validation

### Proof of Concept Guidelines

```python
# Always demonstrate impact SAFELY

# SQL injection PoC
# DON'T: Extract actual data
# DO: Prove injection with sleep
payload = "' OR SLEEP(5)--"

# DON'T: Delete/modify production data
# DO: Show you COULD with SELECT
payload = "' UNION SELECT 'proof_of_concept'--"
```

### Rules of Engagement

1. **Scope verification** - Only test authorized targets
2. **Time windows** - Respect testing hours
3. **DoS prevention** - Avoid resource exhaustion
4. **Data handling** - Don't exfiltrate real data
5. **Stop on discovery** - Don't exploit beyond proof
6. **Immediate reporting** - Report critical findings ASAP
7. **Documentation** - Record all actions
8. **Cleanup** - Remove test artifacts

## Vulnerability Classification

### Severity Scoring

| Severity | Exploitability | Impact | CVSS Range |
|----------|---------------|---------|------------|
| Critical | Easy | Full compromise | 9.0-10.0 |
| High | Medium | Significant access | 7.0-8.9 |
| Medium | Hard | Limited access | 4.0-6.9 |
| Low | Very hard | Minimal impact | 0.1-3.9 |

### Impact Assessment

- **Critical**: Remote code execution, full data access, admin takeover
- **High**: Authentication bypass, privilege escalation, sensitive data exposure
- **Medium**: CSRF, XSS (non-admin), information disclosure
- **Low**: Missing security headers, verbose errors, rate limiting issues

## Testing Checklist

### OWASP Top 10 Coverage

- [ ] Broken Access Control (IDOR, path traversal)
- [ ] Cryptographic Failures (weak encryption, plaintext)
- [ ] Injection (SQL, XSS, command)
- [ ] Insecure Design (missing auth flows)
- [ ] Security Misconfiguration (defaults, debug mode)
- [ ] Vulnerable Components (outdated dependencies)
- [ ] Authentication Failures (weak passwords, session issues)
- [ ] Data Integrity (deserialization, lack of verification)
- [ ] Logging Failures (missing logs, exposed sensitive data)
- [ ] SSRF (unvalidated URLs)

## Quick Reference

| Test Type | Tools | Focus |
|-----------|-------|-------|
| Web App | Burp Suite, OWASP ZAP | OWASP Top 10 |
| API | Postman, curl | AuthN/AuthZ, data exposure |
| Network | nmap, Metasploit | Services, exploits |
| Mobile | MobSF, Frida | Data storage, crypto |
| Cloud | ScoutSuite, Prowler | Misconfigurations |

| Finding Type | Validation Method | Evidence Required |
|--------------|------------------|-------------------|
| SQL Injection | Sleep-based, error-based | Request/response, timing |
| XSS | Alert box, DOM manipulation | Screenshot, payload |
| IDOR | Access other user's resource | Two user accounts, IDs |
| Auth Bypass | Unauthorized access | Before/after screenshots |
| RCE | Command output (safe) | Whoami, id command output |
