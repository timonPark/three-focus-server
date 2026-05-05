# Skills Release Checklist

## 1. Update Version and Counts

Edit `version.json`, then run:
```bash
python scripts/update-docs.py          # compute counts + update all docs
python scripts/update-docs.py --check  # CI: verify files are in sync
python scripts/update-docs.py --dry-run
```

## 2. Update CHANGELOG.md

```markdown
## [X.Y.Z] - YYYY-MM-DD

### Added / Changed / Fixed
- ...

[X.Y.Z]: https://github.com/jeffallan/claude-skills/compare/vPREVIOUS...vX.Y.Z
```

## 3. Update Documentation

- **New skills** → add to `SKILLS_GUIDE.md` + decision trees, run `update-docs.py`
- **New commands** → add to `docs/WORKFLOW_COMMANDS.md` + `README.md` table, run `update-docs.py`
- **Modified** → update cross-references + `SKILLS_GUIDE.md` if triggers changed

## 4. Generate Social Preview

```bash
npm install --no-save puppeteer && node ./assets/capture-screenshot.js
```

Produces `assets/social-preview.png` from `assets/social-preview.html`.

## 5. Validate Skills Integrity

```bash
python scripts/validate-skills.py
python scripts/validate-skills.py --check yaml
python scripts/validate-skills.py --check references
python scripts/validate-skills.py --skill react-expert
python scripts/validate-skills.py --format json
```

Validates: YAML frontmatter, name format, description (max 1024 chars, "Use when" clause), references, count consistency. Exit 0 = OK, 1 = errors.

## 6. Validate Markdown Syntax

```bash
python scripts/validate-markdown.py
python scripts/validate-markdown.py --check    # CI mode
python scripts/validate-markdown.py --path FILE
```

Validates: HTML comments in tables, unclosed code blocks, missing table separators, column count consistency. Exit 0 = no issues, 1 = issues found.

## 7. Final Verification

```bash
# Check no old version references remain (except historical changelog)
grep -r "OLD_VERSION" --include="*.md" --include="*.json" --include="*.html"
```
