<!-- part 1/2 of receiving-feedback.md -->

# Receiving Feedback

---

## Core Mindset

> "Verify before implementing. Ask before assuming. Technical correctness over social comfort."

Code review feedback is a technical discussion, not a social one. Focus on the code, not on feelings.

---

## The Six-Step Process

### Step 1: Read Completely

**Without reacting.** Read the entire comment before forming any response.

```markdown
❌ BAD: Read first sentence → start typing defense
✅ GOOD: Read entire comment → understand full context → then respond
```

### Step 2: Restate Requirements

Rephrase the reviewer's feedback in your own words to confirm understanding.

```markdown
Reviewer: "This function is doing too much. It handles validation,
transformation, and persistence all in one place."

Your restatement: "You're suggesting I split this into three separate
functions: validate(), transform(), and persist()?"
```

### Step 3: Check Against Codebase

Verify the feedback against actual code conditions before responding.

```typescript
// Reviewer says: "This will throw if user is null"

// Check the code:
function getUsername(user: User): string {
  return user.name;  // No null check - reviewer is correct
}

// Or discover context:
function getUsername(user: User): string {
  return user.name;  // TypeScript enforces User, null not possible
}
```

### Step 4: Evaluate Technical Soundness

Consider whether the feedback applies to your specific stack and context.

```markdown
Reviewer: "You should use useMemo here for performance"

Evaluate:
- Is this component re-rendering frequently? → Check React DevTools
- Is the computation expensive? → Profile it
- Does React 19's compiler auto-optimize this? → Check version
```

### Step 5: Respond with Substance

Provide technical acknowledgment or reasoned objection.

```markdown
✅ GOOD: "Fixed. Split into validate(), transform(), persist()
         at lines 24, 45, 67."

✅ GOOD: "Respectfully disagree. This list has max 5 items
         (see schema.ts:12), so filter performance is O(5)."

❌ BAD: "You're absolutely right! Great catch!"
❌ BAD: "I don't think that's necessary."
```

### Step 6: Implement One at a Time

Address each piece of feedback individually with verification.

```markdown
Feedback item 1: Add null check
→ Implement → Test → Commit → Verify → Move to next

Feedback item 2: Extract helper function
→ Implement → Test → Commit → Verify → Move to next

NOT: Try to address all feedback in one massive commit
```

---

## Avoiding Agreement Theater

### The Problem

Performative agreement wastes time and provides no information. When you write "Great point!" you're adding noise, not signal.

### Forbidden Phrases

| Phrase | Why It's Wrong |
|--------|----------------|
| "You're absolutely right!" | Sycophantic, adds no information |
| "Great point!" | Empty praise, not a response |
| "Excellent feedback!" | Flattery, not engagement |
| "Thanks for catching this!" | Unnecessary, just fix it |
| "I really appreciate..." | Social fluff, not technical |

### Actions Demonstrate Understanding

```markdown
❌ "You're absolutely right! Great catch on that null check!
    Thanks so much for pointing this out!"

✅ "Fixed. Added null check at line 42."
```

The code change shows you understood. Words are redundant.

### When Acknowledgment IS Appropriate

Brief, technical acknowledgment when learning something new:

```markdown
✅ "I wasn't aware of that edge case. Added handling at line 42."
✅ "Good point about thread safety. Added mutex at line 67."
```

---

## When to Push Back

