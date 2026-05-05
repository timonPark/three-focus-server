<!-- part 1/2 of systematic-debugging.md -->

# Systematic Debugging

---

## Core Principle

> **NO FIXES WITHOUT ROOT CAUSE INVESTIGATION FIRST.**

Jumping to fixes without understanding causes creates more bugs. Systematic debugging prevents the "fix one thing, break two more" cycle.

---

## The Four Mandatory Phases

```
┌─────────────────────────────────────────────────────────────┐
│                    SYSTEMATIC DEBUGGING                      │
├─────────────────────────────────────────────────────────────┤
│  Phase 1: ROOT CAUSE INVESTIGATION                          │
│  ├── Read error messages thoroughly                         │
│  ├── Reproduce reliably with documented steps               │
│  ├── Examine recent changes                                 │
│  └── Trace data flow backward                               │
├─────────────────────────────────────────────────────────────┤
│  Phase 2: PATTERN ANALYSIS                                   │
│  ├── Find similar working implementations                   │
│  ├── Study reference implementations completely             │
│  └── Document all differences                               │
├─────────────────────────────────────────────────────────────┤
│  Phase 3: HYPOTHESIS TESTING                                 │
│  ├── Form specific, written hypothesis                      │
│  ├── Test with minimal, isolated changes                    │
│  └── One variable at a time                                 │
├─────────────────────────────────────────────────────────────┤
│  Phase 4: IMPLEMENTATION                                     │
│  ├── Create failing test case                               │
│  ├── Implement single fix addressing root cause             │
│  └── Verify no new breakage                                 │
└─────────────────────────────────────────────────────────────┘
```

---

## Phase 1: Root Cause Investigation

**Objective:** Understand exactly what is failing and why before attempting any fix.

### Step 1.1: Read Error Messages Thoroughly

```bash
# Don't just read the first line
TypeError: Cannot read property 'map' of undefined
    at UserList.render (UserList.tsx:24)
    at renderWithHooks (react-dom.js:14985)
    at mountIndeterminateComponent (react-dom.js:17811)
```

**Key questions:**
- What exact operation failed?
- Where in the code (file, line)?
- What was the call stack?
- Are there multiple errors or just one?

### Step 1.2: Reproduce Reliably

```markdown
## Reproduction Steps
1. Navigate to /users
2. Click "Load More" button
3. Wait for loading spinner
4. **ERROR: "Cannot read property 'map' of undefined"**

## Environment
- Browser: Chrome 120
- User: Admin role
- Data state: 50+ users in database
```

**Requirement:** Document exact steps that reproduce the bug 100% of the time.

### Step 1.3: Examine Recent Changes

```bash
# What changed recently?
git log --oneline -10

# What specifically changed in the failing file?
git log -p UserList.tsx

# When did this start failing?
git bisect start
git bisect bad HEAD
git bisect good v1.2.0
```

### Step 1.4: Trace Data Flow Backward

```typescript
// Error happens here:
users.map(u => u.name)  // users is undefined

// Trace backward:
// Where does 'users' come from?
const users = props.users;

// Where do props come from?
<UserList users={data.users} />

// Where does data come from?
const { data } = useQuery(GET_USERS);

// ROOT CAUSE: Query returns { users: null } when loading
```

### Step 1.5: Add Diagnostic Instrumentation

```typescript
// Add temporary logging at boundaries
console.log('[UserList] props:', JSON.stringify(props));
console.log('[UserList] users type:', typeof props.users);
console.log('[UserList] users value:', props.users);

// Check at data source
console.log('[API] Response:', response);
console.log('[API] Response.data:', response.data);
```

---

## Phase 2: Pattern Analysis

**Objective:** Find working examples to understand what correct behavior looks like.

### Step 2.1: Locate Similar Working Implementations

```bash
# Find similar components that work correctly
grep -r "useQuery" src/components/ --include="*.tsx"

# Find how other lists handle loading states
grep -r "loading" src/components/*List* --include="*.tsx"
```

### Step 2.2: Study Reference Implementations Completely

```typescript
// WORKING: ProductList.tsx
function ProductList({ products, loading }) {
  if (loading) return <Spinner />;
  if (!products) return null;  // ← Handles undefined case

  return products.map(p => <ProductItem key={p.id} {...p} />);
}

// BROKEN: UserList.tsx
function UserList({ users, loading }) {
  if (loading) return <Spinner />;
  // Missing: !users check

  return users.map(u => <UserItem key={u.id} {...u} />);  // 💥 Crashes
}
```

### Step 2.3: Document All Differences

| Aspect | Working (ProductList) | Broken (UserList) |
|--------|----------------------|-------------------|
| Null check | `if (!products)` | Missing |
| Default value | `products ?? []` | None |
| Loading handled | Before render | Before render |
| Error handled | Returns ErrorState | Missing |

---

## Phase 3: Hypothesis Testing
