# ThreeFocus Server Project Rules

## Work History Tracking

모든 작업 세션이 끝날 때마다 `work-history/YYYY-MM-DD.md`를 생성하거나 업데이트한다. 날짜는 `currentDate` 컨텍스트를 사용한다.

**파일 구조:**
```markdown
# Work History — YYYY-MM-DD

## 컨텍스트
사용자 요청 요약

## 작업 내역
구현한 내용과 파일 경로

## API 변경 사항
추가/변경된 API (Method, URL, Request/Response 구조)

## DB 변경 사항
추가된 마이그레이션 파일 내용

## 테스트
추가/수정된 테스트 파일

## 다음 세션 참고사항
미해결 이슈, 알려진 문제, 다음에 해야 할 것
```

**규칙:**
- 같은 날짜에 여러 세션이 있으면 동일 파일에 누적 추가
- API 경로, 요청/응답 구조의 변경사항은 반드시 기록
- 엔티티 구조 변경 시 업데이트된 구조를 간략히 기록

---

# Claude Skills Project Configuration

> This file governs Claude's behavior when working on the claude-skills repository.

---

## Skill Authorship Standards

Skills follow the [Agent Skills specification](https://agentskills.io/specification). This section covers project-specific conventions that go beyond the base spec.

### The Description Trap

**Critical:** Never put process steps or workflow sequences in descriptions. When descriptions contain step-by-step instructions, agents follow the brief description instead of reading the full skill content. This defeats the purpose of detailed skills.

Brief capability statements (what it does) and trigger conditions (when to use it) are both appropriate. Process steps (how it works) are not.

**BAD - Process steps in description:**
```yaml
description: Use for debugging. First investigate root cause, then analyze patterns, test hypotheses, and implement fixes with tests.
```

**GOOD - Capability + trigger:**
```yaml
description: Diagnoses bugs through root cause analysis and pattern matching. Use when encountering errors or unexpected behavior requiring investigation.
```

**Format:** `[Brief capability statement]. Use when [triggering conditions].`

Descriptions tell WHAT the skill does and WHEN to use it. The SKILL.md body tells HOW.

---

### Frontmatter Requirements

Per the [Agent Skills specification](https://agentskills.io/specification), only `name` and `description` are top-level required fields. Custom fields go under `metadata`.

```yaml
---
name: skill-name-with-hyphens
description: [Brief capability statement]. Use when [triggering conditions] - max 1024 chars
license: MIT
metadata:
  author: https://github.com/Jeffallan
  version: "1.0.0"
  domain: frontend
  triggers: keyword1, keyword2, keyword3
  role: specialist
  scope: implementation
  output-format: code
  related-skills: fullstack-guardian, test-master, devops-engineer
---
```

**Top-level fields (spec-defined):**
- `name`: Letters, numbers, and hyphens only (no parentheses or special characters)
- `description`: Maximum 1024 characters. Capability statement + trigger conditions. No process steps.
- `license`: Always `MIT` for this project
- `allowed-tools`: Space-delimited tool list (only on skills that restrict tools)

**Metadata fields (project-specific):**
- `author`: GitHub profile URL of the skill author
- `version`: Semantic version string (quoted, e.g., `"1.0.0"`)
- `domain`: Category from the domain list below
- `triggers`: Comma-separated searchable keywords
- `role`: `specialist` | `expert` | `architect` | `engineer`
- `scope`: `implementation` | `review` | `design` | `system-design` | `testing` | `analysis` | `infrastructure` | `optimization` | `architecture`
- `output-format`: `code` | `document` | `report` | `architecture` | `specification` | `schema` | `manifests` | `analysis` | `analysis-and-code` | `code+analysis`
- `related-skills`: Comma-separated skill directory names (e.g., `fullstack-guardian, test-master`). Must resolve to existing skill directories.

**Domain values:**
`language` · `backend` · `frontend` · `infrastructure` · `api-architecture` · `quality` · `devops` · `security` · `data-ml` · `platform` · `specialized` · `workflow`

---

### Reference File Standards

Reference files follow the [Agent Skills specification](https://agentskills.io/specification). No specific headers are required.

**Guidelines:**
- 100-600 lines per reference file
- Keep files focused on a single topic
- Complete, working code examples with TypeScript types
- Cross-reference related skills where relevant
- Include "when to use" and "when not to use" guidance
- Practical patterns over theoretical explanations

### Framework Idiom Principle

Reference files for framework-specific skills must reflect the idiomatic best practices of that framework, not generic patterns applied uniformly across all skills. If a framework provides a built-in mechanism (e.g., global error handling, middleware, dependency injection), reference examples should use it rather than duplicating that behavior manually. Each framework's conventions for error handling, architecture, and code organization take precedence over cross-project consistency.

---

### Progressive Disclosure Architecture

**Tier 1 - SKILL.md (~80-100 lines)**
- Role definition and expertise level
- When-to-use guidance (triggers)
- Core workflow (5 steps)
- Constraints (MUST DO / MUST NOT DO)
- Routing table to references

**Tier 2 - Reference Files (100-600 lines each)**
- Deep technical content
- Complete code examples
- Edge cases and antipatterns
- Loaded only when context requires

**Goal:** 50% token reduction through selective loading.

---

## Project Workflow

### When Creating New Skills

1. Check existing skills for overlap
2. Write SKILL.md with capability + trigger description (no process steps)
3. Create reference files for deep content (100+ lines)
4. Add a routing table linking topics to references
5. Test skill triggers with realistic prompts
6. Update SKILLS_GUIDE.md if adding a new domain

### When Modifying Skills

1. Read the full current skill before editing
2. Maintain capability + trigger description format (no process steps)
3. Preserve progressive disclosure structure
4. Update related cross-references
5. Verify routing table accuracy

---

## Release Checklist

When releasing a new version, follow these steps.

### 1. Update Version and Counts

Version and counts are managed through `version.json`:

```json
{
  "version": "0.4.2",
  "skillCount": 65,
  "workflowCount": 9,
  "referenceFileCount": 355
}
```

**To release a new version:**

1. Update the `version` field in `version.json`
2. Run the update script:

```bash
python scripts/update-docs.py
```

The script will:
- Compute counts from the filesystem (skills, references, workflows)
- Update `version.json` with computed counts
- Update all documentation files (README.md, plugin.json, etc.)

**Options:**
```bash
python scripts/update-docs.py --check    # Verify files are in sync (CI use)
python scripts/update-docs.py --dry-run  # Preview changes without writing
```

### 2. Update CHANGELOG.md

Add a new version entry at the top following Keep a Changelog format:

```markdown
## [X.Y.Z] - YYYY-MM-DD

### Added
- New features, skills, commands

### Changed
- Modified functionality, updated skills

### Fixed
- Bug fixes
```

Add a version comparison link at the bottom:
```markdown
[X.Y.Z]: https://github.com/jeffallan/claude-skills/compare/vPREVIOUS...vX.Y.Z
```

### 3. Update Documentation for New/Modified Content

**For new skills:**
- Add to `SKILLS_GUIDE.md` in the appropriate category
- Add to decision trees if applicable
- Run `python scripts/update-docs.py` to update counts

**For new commands:**
- Add to `docs/WORKFLOW_COMMANDS.md`
- Add to `README.md` Project Workflow Commands table
- Run `python scripts/update-docs.py` to update counts

**For modified skills/commands:**
- Update any cross-references
- Update SKILLS_GUIDE.md if triggers changed

### 4. Generate Social Preview

After all updates, regenerate the social preview image:

```bash
npm install --no-save puppeteer && node ./assets/capture-screenshot.js
```

This creates `assets/social-preview.png` from `assets/social-preview.html`.

### 5. Validate Skills Integrity

**Critical:** Run validation before release to prevent broken skills from being published.

```bash
python scripts/validate-skills.py
```

The script validates:
- **YAML frontmatter** - Parsing, required fields (name, description, triggers), format
- **Name format** - Letters, numbers, hyphens only
- **Description** - Max 1024 chars, must contain the "Use when" trigger clause
- **References** - Directory exists, has files, proper headers
- **Count consistency** - Skills/reference counts match across documentation

**Options:**
```bash
python scripts/validate-skills.py --check yaml       # YAML checks only
python scripts/validate-skills.py --check references # Reference checks only
python scripts/validate-skills.py --skill react-expert  # Single skill
python scripts/validate-skills.py --format json      # JSON output for CI
python scripts/validate-skills.py --help             # Full usage
```

**Exit codes:** 0 = success (warnings OK), 1 = errors found

### 6. Validate Markdown Syntax

**Critical:** Run Markdown validation to catch parsing errors.

```bash
python scripts/validate-markdown.py
```

The script validates:
- **HTML comments in tables** - Comments between table rows break parsing
- **Unclosed code blocks** - Ensures all code fences are properly closed
- **Missing table separators** - Tables require `|---|` row after header
- **Column count consistency** - All table rows must have the same column count

**Options:**
```bash
python scripts/validate-markdown.py --check       # CI mode (exit code only)
python scripts/validate-markdown.py --path FILE   # Single file
python scripts/validate-markdown.py --format json # JSON output for CI
```

**Exit codes:** 0 = no issues, 1 = issues found

### 7. Final Verification

After running validation, manually verify:

```bash
# Check no old version references remain (except historical changelog)
grep -r "OLD_VERSION" --include="*.md" --include="*.json" --include="*.html"
```

---

## Attribution

Behavioral patterns and process discipline adapted from:
- **[obra/superpowers](https://github.com/obra/superpowers)** by Jesse Vincent (@obra)
- License: MIT

Research documented in: `research/superpowers.md`
