# Skills Authorship Standards

## The Description Trap

Never put process steps in descriptions. Agents follow the brief description and skip the full skill.

**BAD:**
```yaml
description: Use for debugging. First investigate root cause, then analyze patterns, test hypotheses, and implement fixes with tests.
```
**GOOD:**
```yaml
description: Diagnoses bugs through root cause analysis and pattern matching. Use when encountering errors or unexpected behavior requiring investigation.
```

Format: `[Brief capability statement]. Use when [triggering conditions].`

---

## Frontmatter Reference

```yaml
---
name: skill-name-with-hyphens
description: capability statement. Use when triggering conditions — max 1024 chars
license: MIT
metadata:
  author: https://github.com/Jeffallan
  version: "1.0.0"
  domain: backend
  triggers: keyword1, keyword2, keyword3
  role: specialist
  scope: implementation
  output-format: code
  related-skills: fullstack-guardian, test-master
---
```

**Top-level fields:**
- `name`: letters, numbers, hyphens only
- `description`: max 1024 chars, no process steps
- `license`: always `MIT`
- `allowed-tools`: space-delimited (only when restricting tools)

**Metadata fields:**
- `domain`: `language` · `backend` · `frontend` · `infrastructure` · `api-architecture` · `quality` · `devops` · `security` · `data-ml` · `platform` · `specialized` · `workflow`
- `role`: `specialist` | `expert` | `architect` | `engineer`
- `scope`: `implementation` | `review` | `design` | `system-design` | `testing` | `analysis` | `infrastructure` | `optimization` | `architecture`
- `output-format`: `code` | `document` | `report` | `architecture` | `specification` | `schema` | `manifests` | `analysis` | `analysis-and-code` | `code+analysis`
- `related-skills`: must resolve to existing skill directories

---

## Reference File Standards

- **100–220 lines** per file, single topic
- Complete, working code examples with TypeScript types
- Cross-reference related skills where relevant
- Include "when to use" and "when not to use" guidance
- Practical patterns over theoretical explanations

**Framework Idiom Principle:** Use the framework's built-in mechanisms (global error handling, middleware, DI) rather than duplicating behavior manually. Framework conventions take precedence over cross-project consistency.

---

## Progressive Disclosure Architecture

**Tier 1 — SKILL.md (~80-100 lines)**
- Role definition and expertise level
- When-to-use guidance (triggers)
- Core workflow (5 steps)
- Constraints (MUST DO / MUST NOT DO)
- Routing table to references

**Tier 2 — Reference Files (100–220 lines each)**
- Deep technical content, complete examples, edge cases, antipatterns
- Loaded only when context requires

Goal: 50% token reduction through selective loading.
