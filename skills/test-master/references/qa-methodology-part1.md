<!-- part 1/2 of qa-methodology.md -->

# QA Methodology

## Manual Testing Types

### Exploratory Testing
```markdown
**Charter**: Explore {feature} with focus on {aspect}
**Duration**: 60-90 min
**Mission**: Find defects in {specific functionality}

Test Ideas:
- Boundary conditions & edge cases
- Error handling & recovery
- User workflow variations
- Integration points

Findings:
1. [HIGH] {Issue + impact}
2. [MED] {Issue + impact}

Coverage: {Areas explored} | Risks: {Identified risks}
```

### Usability Testing
```markdown
**Task**: Can users complete {action} intuitively?
**Metrics**: Time to complete, errors made, satisfaction (1-5)
**Success**: 80% complete without help in <5 min

Observations:
- Navigation confusing at {step}
- Users expect {A} but get {B}
- Positive: {feature feedback}
```

### Accessibility Testing (WCAG 2.1 AA)
```typescript
test('accessibility compliance', async ({ page }) => {
  // Keyboard navigation
  await page.keyboard.press('Tab');
  expect(['A', 'BUTTON', 'INPUT']).toContain(
    await page.evaluate(() => document.activeElement.tagName)
  );
  
  // ARIA labels
  expect(await page.getByRole('button').first().getAttribute('aria-label')).toBeTruthy();
  
  // Color contrast (axe-core)
  const violations = await page.evaluate(async () => {
    const axe = await import('axe-core');
    return (await axe.run()).violations;
  });
  expect(violations).toHaveLength(0);
});
```

### Localization Testing
```markdown
**Test**: {Feature} in {language/locale}
- [ ] Text displays without truncation
- [ ] Date/time/currency formats correct
- [ ] Right-to-left layout (Arabic, Hebrew)
- [ ] Character encoding UTF-8
- [ ] Sort order respects locale
```

### Compatibility Matrix
```markdown
| Browser | Version | OS | Status |
|---------|---------|----|----- --|
| Chrome | Latest | Win/Mac | ✓ |
| Firefox | Latest | Win/Mac | ✓ |
| Safari | Latest | macOS/iOS | ✓ |
| Edge | Latest | Windows | ✓ |
```

## Test Design Techniques

### Pairwise Testing
```typescript
// Test all parameter pairs efficiently
const pairwiseTests = [
  { browser: 'chrome', os: 'windows', lang: 'en' },
  { browser: 'firefox', os: 'mac', lang: 'es' },
  { browser: 'safari', os: 'windows', lang: 'fr' },
  // Covers all pairs with minimal tests
];
```

### Risk-Based Testing
```markdown
| Risk | Probability | Impact | Priority | Test Effort |
|------|-------------|--------|----------|-------------|
| Critical | High | High | P0 | Exhaustive |
| High | Med-High | High | P1 | Comprehensive |
| Medium | Low-Med | Med | P2 | Standard |
| Low | Low | Low | P3 | Smoke only |
```

## Defect Management

### Root Cause Analysis (5 Whys)
```markdown
1. Why did defect occur? {User input not validated}
2. Why wasn't it validated? {Validation logic missing}
3. Why was it missing? {Requirement unclear}
4. Why was requirement unclear? {Acceptance criteria incomplete}
5. Why incomplete? {No QA review in planning}

**Root Cause**: QA not involved in requirements phase
**Prevention**: Add QA to all planning meetings
```

### Defect Report Template
```markdown
## [CRITICAL] {Defect Title}

**Steps to Reproduce**:
1. {Step 1}
2. {Step 2}

**Expected**: {Should happen}
**Actual**: {Actually happens}
**Impact**: {Business/user impact}
**Root Cause**: {Why it happened}
**Fix**: {Recommended solution}
```

