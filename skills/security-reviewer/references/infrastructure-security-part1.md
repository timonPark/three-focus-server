<!-- part 1/2 of infrastructure-security.md -->

# Infrastructure Security

## DevSecOps Integration

### CI/CD Security Pipeline

```yaml
# GitHub Actions - Security scanning
name: Security Pipeline
on: [push, pull_request]
jobs:
  security:
    runs-on: ubuntu-latest
    steps:
      - uses: returntocorp/semgrep-action@v1
      - uses: gitleaks/gitleaks-action@v2
      - uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          severity: 'CRITICAL,HIGH'
```

### Infrastructure as Code Security

```bash
# Terraform/CloudFormation scanning
checkov -d terraform/ --framework terraform
tfsec terraform/
terrascan scan -d terraform/

# Kubernetes manifest scanning
kubesec scan deployment.yaml
```

## Cloud Security Controls

### AWS Security Hardening

```bash
# Enable security services
aws guardduty create-detector --enable
aws securityhub enable-security-hub
aws cloudtrail create-trail --name security-trail --s3-bucket-name logs

# Check S3 bucket security
aws s3api list-buckets --query "Buckets[].Name" | \
  xargs -I {} aws s3api get-bucket-acl --bucket {}

# IAM password policy
aws iam update-account-password-policy \
  --minimum-password-length 14 \
  --require-symbols --require-numbers \
  --require-uppercase-characters --require-lowercase-characters
```

### Azure Security

```bash
# Enable Security Center
az security auto-provisioning-setting update --name default --auto-provision on

# Enable disk encryption
az vm encryption enable --resource-group myRG --name myVM --disk-encryption-keyvault myKV
```

### GCP Security

```bash
# Enable Security Command Center
gcloud services enable securitycenter.googleapis.com

# Enable VPC Flow Logs
gcloud compute networks subnets update SUBNET --enable-flow-logs
```

## Container Security

### Secure Dockerfile

```dockerfile
FROM node:18-alpine
RUN addgroup -g 1001 -S nodejs && adduser -S nodejs -u 1001
WORKDIR /app
COPY --chown=nodejs:nodejs package*.json ./
RUN npm ci --only=production
USER nodejs
EXPOSE 3000
HEALTHCHECK --interval=30s CMD node healthcheck.js
CMD ["node", "server.js"]
```

### Kubernetes Security

```yaml
# Pod Security Standards
apiVersion: v1
kind: Pod
metadata:
  name: secure-pod
spec:
  securityContext:
    runAsNonRoot: true
    runAsUser: 1000
    fsGroup: 2000
    seccompProfile:
      type: RuntimeDefault
  containers:
  - name: app
    image: myapp:1.0
    securityContext:
      allowPrivilegeEscalation: false
      readOnlyRootFilesystem: true
      capabilities:
        drop: [ALL]
    resources:
      limits:
        memory: "128Mi"
        cpu: "500m"
---
# Network Policy - Default deny
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-all
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
```

## Compliance Automation
