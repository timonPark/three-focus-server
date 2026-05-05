<!-- part 1/2 of spec-compliance-review.md -->

# Spec Compliance Review

---

## Two-Stage Review Architecture

```
                    ┌─────────────────────┐
                    │   Implementation    │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  STAGE 1: Spec      │
                    │  Compliance Review  │
                    └──────────┬──────────┘
                               │
              ┌────────────────┴────────────────┐
              │                                  │
      ┌───────▼───────┐                ┌────────▼────────┐
      │   ✗ Issues    │                │   ✓ Compliant   │
      │     Found     │                │                 │
      └───────┬───────┘                └────────┬────────┘
              │                                  │
              │                        ┌────────▼────────┐
              │                        │  STAGE 2: Code  │
              │                        │  Quality Review │
              │                        └────────┬────────┘
              │                                  │
              │                    ┌─────────────┴─────────────┐
              │                    │                           │
              │            ┌───────▼───────┐         ┌────────▼────────┐
              │            │   ✗ Issues    │         │   ✓ Approved    │
              │            │     Found     │         │                 │
              │            └───────┬───────┘         └─────────────────┘
              │                    │
              └────────────────────┴────────────────────┐
                                                        │
                                              ┌─────────▼─────────┐
                                              │ Return to Author  │
                                              └───────────────────┘
```

**Critical:** Complete Stage 1 (spec compliance) BEFORE Stage 2 (code quality). Never review code quality for functionality that doesn't meet the specification.

---

## Stage 1: Spec Compliance Review

### Core Directive

> "The implementer finished suspiciously quickly. Their report may be incomplete, inaccurate, or optimistic."

Approach every review with professional skepticism. Verify claims independently.

### The Three Verification Categories

#### Category 1: Missing Requirements

**Check for features that were requested but not implemented.**

| Question | How to Verify |
|----------|---------------|
| Did they skip requested features? | Compare PR to original requirements line by line |
| Are edge cases handled? | Check error paths, empty states, boundaries |
| Were error scenarios addressed? | Look for try/catch, error boundaries, validation |
| Is the happy path complete? | Trace through primary use case manually |

```markdown
## Example Review Finding

**Missing Requirement:** Issue #42 requested "password must be at least 8 characters"

**Found in code:**
```typescript
// No length validation present
function validatePassword(password: string) {
  return password.length > 0;  // Only checks non-empty
}
```

**Status:** ❌ Incomplete - minimum length validation missing
```

#### Category 2: Unnecessary Additions

**Check for scope creep and over-engineering.**

| Question | How to Verify |
|----------|---------------|
| Features beyond specification? | Compare to original requirements |
| Over-engineering? | Is complexity justified by requirements? |
| Premature optimization? | Is performance cited without measurements? |
| Unrequested abstractions? | Are there helpers/utils for one-time use? |

```markdown
## Example Review Finding

**Unnecessary Addition:** Added caching layer not in requirements

**Found in code:**
```typescript
// Original requirement: "Fetch user by ID"
// Actual implementation:
class CachedUserRepository {  // Not requested
  private cache = new Map();
  private ttl = 60000;

  async getUser(id: string) {
    if (this.cache.has(id)) { ... }
    // 50 lines of cache logic
  }
}
```

**Status:** ⚠️ Scope creep - discuss before merging
```

#### Category 3: Interpretation Gaps

**Check for misunderstandings of requirements.**

| Question | How to Verify |
|----------|---------------|
| Different understanding of requirements? | Ask author to explain their interpretation |
| Unclarified assumptions? | Look for comments like "assuming..." |
| Ambiguous specs resolved incorrectly? | Compare to similar existing features |

```markdown
## Example Review Finding

**Interpretation Gap:** "Sort by date" implemented as ascending

**Requirement stated:** "Sort by date" (ambiguous)

**Author implemented:** Oldest first (ascending)

**Expected:** Most recent first is typical UX pattern

**Status:** ❓ Clarify - which sort order was intended?
```

---
