<!-- part 2/2 of systematic-debugging.md -->


**Objective:** Verify your understanding with controlled experiments.

### Step 3.1: Form Specific, Written Hypothesis

```markdown
## Hypothesis #1
**Statement:** The crash occurs because `users` is undefined when the
query is complete but returns no data.

**Prediction:** Adding a null check before `.map()` will prevent the crash.

**Test:** Add `if (!users) return null;` before the map call.
```

### Step 3.2: Test with Minimal Changes

```typescript
// Change ONLY one thing
function UserList({ users, loading }) {
  if (loading) return <Spinner />;
  if (!users) return null;  // ← Single change

  return users.map(u => <UserItem key={u.id} {...u} />);
}
```

### Step 3.3: One Variable at a Time

```markdown
## Test Results

| Hypothesis | Change | Result | Conclusion |
|------------|--------|--------|------------|
| #1: Null check | Add `if (!users)` | ✓ Pass | Confirmed |

Do NOT test multiple hypotheses simultaneously.
```

---

## Phase 4: Implementation

**Objective:** Fix the bug permanently with proper safeguards.

### Step 4.1: Create Failing Test Case First

```typescript
describe('UserList', () => {
  it('should handle undefined users gracefully', () => {
    // This test should FAIL before the fix
    const { container } = render(<UserList users={undefined} loading={false} />);
    expect(container).not.toThrow();
    expect(screen.queryByRole('list')).not.toBeInTheDocument();
  });
});
```

### Step 4.2: Implement Single Fix

```typescript
function UserList({ users, loading }: UserListProps) {
  if (loading) return <Spinner />;
  if (!users || users.length === 0) {
    return <EmptyState message="No users found" />;
  }

  return (
    <ul role="list">
      {users.map(u => <UserItem key={u.id} {...u} />)}
    </ul>
  );
}
```

### Step 4.3: Verify No New Breakage

```bash
# Run full test suite
npm test

# Run specific component tests
npm test UserList

# Run integration tests
npm run test:integration

# Verify in browser
# 1. Normal case: 50 users
# 2. Empty case: 0 users
# 3. Loading case: spinner shows
# 4. Error case: error message shows
```

---

## The Three-Fix Threshold

> **After 3 failed fix attempts → STOP.**

Three failures in different locations signals architectural problems, not isolated bugs.

### What Three Failures Means

```
Fix Attempt 1: Added null check → New error in child component
Fix Attempt 2: Fixed child component → New error in parent
Fix Attempt 3: Fixed parent → Original error returns
                              ↓
                    STOP. QUESTION ARCHITECTURE.
```

### At the Threshold, Do This

1. **Stop fixing symptoms**
2. **Document the pattern** of failures
3. **Identify architectural assumptions** being violated
4. **Propose structural change** rather than patch
5. **Discuss with team** before proceeding

---

## Red Flags Requiring Process Reset

When you notice these, stop and restart from Phase 1:

| Red Flag | Why It's Wrong |
|----------|----------------|
| Proposing solutions before tracing data flow | Guessing, not debugging |
| Making multiple simultaneous changes | Can't identify which change worked |
| Skipping test creation | Bug will recur |
| "Let's try this and see if it works" | Shotgun debugging |
| Fixing without understanding the cause | Band-aid, not cure |

---

## Decision Flowchart

```
                    ┌──────────────────┐
                    │   Bug Reported   │
                    └────────┬─────────┘
                             │
              ┌──────────────▼──────────────┐
              │   Can you reproduce it?      │
              └──────────────┬──────────────┘
                    No       │       Yes
            ┌────────────────┴────────────────┐
            ▼                                  ▼
    ┌───────────────┐               ┌─────────────────┐
    │ Get more info │               │ Trace data flow │
    └───────────────┘               └────────┬────────┘
                                             │
                              ┌──────────────▼──────────────┐
                              │ Do you understand the cause? │
                              └──────────────┬──────────────┘
                                    No       │       Yes
                    ┌────────────────────────┴─────────┐
                    ▼                                   ▼
            ┌───────────────┐               ┌─────────────────┐
            │ Study working │               │ Write hypothesis│
            │   examples    │               └────────┬────────┘
            └───────────────┘                        │
                                             ┌───────▼───────┐
                                             │  Write test   │
                                             └───────┬───────┘
                                                     │
                                             ┌───────▼───────┐
                                             │  Implement    │
                                             └───────┬───────┘
                                                     │
                                  ┌──────────────────▼──────────────────┐
                                  │          Does test pass?            │
                                  └──────────────────┬──────────────────┘
                                            No       │       Yes
                            ┌────────────────────────┴──────────┐
                            ▼                                    ▼
                    ┌───────────────┐                  ┌─────────────────┐
                    │ Attempt < 3?  │                  │      Done       │
                    └───────┬───────┘                  └─────────────────┘
                    No      │      Yes
            ┌───────────────┴─────────────────┐
            ▼                                  ▼
    ┌───────────────────┐          ┌─────────────────────┐
    │ Question          │          │ Return to Phase 1   │
    │ architecture      │          └─────────────────────┘
    └───────────────────┘
```

---

*Content adapted from [obra/superpowers](https://github.com/obra/superpowers) by Jesse Vincent (@obra), MIT License.*
