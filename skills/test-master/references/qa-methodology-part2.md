<!-- part 2/2 of qa-methodology.md -->

## Quality Metrics

### Key Calculations
```typescript
// Defect Removal Efficiency (target: >95%)
const dre = (defectsInTesting / (defectsInTesting + defectsInProd)) * 100;

// Defect Leakage (target: <5%)
const leakage = (defectsInProd / totalDefects) * 100;

// Test Effectiveness (target: >90%)
const effectiveness = (defectsFoundByTests / totalDefects) * 100;

// Automation ROI
const roi = (timeSaved - maintenanceCost - developmentCost) / developmentCost;
```

### Quality Dashboard
```markdown
| Metric | Target | Actual | Trend | Status |
|--------|--------|--------|-------|--------|
| Coverage | >80% | 87% | ↑ | ✓ |
| Defect Leakage | <5% | 3% | ↓ | ✓ |
| Automation | >70% | 68% | ↑ | ⚠ |
| Critical Defects | 0 | 0 | → | ✓ |
| MTTR | <48h | 36h | ↓ | ✓ |
```

## Continuous Testing & Shift-Left

### Shift-Left Activities
```markdown
**Early Testing**:
- Review requirements for testability
- Create test cases during design
- TDD: unit tests with code
- Automated tests in CI pipeline
- Static analysis on commit
- Security scanning pre-merge

**Benefits**: 10x cheaper defect fixes, faster feedback
```

### Feedback Cycle Targets
```typescript
const feedbackCycle = {
  unitTests: '< 5 min',       // On save
  integration: '< 15 min',    // On commit
  e2e: '< 30 min',            // On PR
  regression: '< 2 hours',    // Nightly
};
```

## Quality Advocacy

### Quality Gates
```markdown
## Production Release Gate

**Must Pass (Blockers)**:
- [ ] Zero critical defects
- [ ] Coverage >80%
- [ ] All P0/P1 tests passing
- [ ] Performance SLA met
- [ ] Security scan clean
- [ ] Accessibility WCAG AA

**Decision**: GO | NO-GO | GO with exceptions
```

### Team Education Program
```markdown
**Week 1-2**: Test fundamentals
**Week 3-4**: Automation basics
**Week 5-6**: Advanced topics (perf, security, API)
**Ongoing**: Best practices, tool updates
```

## Test Planning

### Test Plan Template
```markdown
## Test Plan: {Feature}

**Scope**: {What to test}
**Types**: Unit, Integration, E2E, Perf, Security
**Resources**: {Team allocation}
**Dependencies**: {Prerequisites}
**Schedule**: {Timeline}
**Entry Criteria**: {Start conditions}
**Exit Criteria**: {Completion conditions}
**Risks**: {Identified risks + mitigation}
```

### Environment Strategy
```markdown
| Env | Purpose | Data | Refresh | Access |
|-----|---------|------|---------|--------|
| Dev | Development | Synthetic | On-demand | All |
| Test | QA testing | Test data | Daily | QA |
| Stage | Pre-prod | Prod-like | Weekly | Limited |
| Prod | Live | Real | N/A | Ops |
```

## Quick Reference

| Testing Type | When | Duration |
|--------------|------|----------|
| Exploratory | New features | 60-120 min |
| Usability | UI changes | 2-4 hours |
| Accessibility | Every release | 1-2 hours |
| Localization | Multi-region | 1 day/locale |

| Metric | Excellent | Good | Needs Work |
|--------|-----------|------|------------|
| Coverage | >90% | 70-90% | <70% |
| Leakage | <2% | 2-5% | >5% |
| Automation | >80% | 60-80% | <60% |
| MTTR | <24h | 24-48h | >48h |
