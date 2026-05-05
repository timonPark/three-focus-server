# ThreeFocus Server Project Rules

## Work History Tracking

모든 작업 세션이 끝날 때마다 `work-history/YYYY-MM-DD.md`를 생성하거나 업데이트한다. 날짜는 `currentDate` 컨텍스트를 사용한다.

**파일 구조:** `## 컨텍스트` / `## 작업 내역` / `## API 변경 사항` / `## DB 변경 사항` / `## 테스트` / `## 다음 세션 참고사항`

**규칙:**
- 같은 날짜에 여러 세션이 있으면 동일 파일에 누적 추가
- API 경로·요청/응답 구조 변경사항 반드시 기록
- 엔티티 구조 변경 시 업데이트된 구조 간략히 기록

---

# Claude Skills Project Configuration

> 상세 내용: `docs/skills-authorship.md` (작성 표준) · `docs/skills-release.md` (릴리즈 체크리스트)

## Skill Authorship Standards

**Description 규칙:** `[capability statement]. Use when [triggering conditions].` — 프로세스 단계 금지.

**Frontmatter (필수):**
```yaml
---
name: skill-name-with-hyphens        # 하이픈·영숫자만
description: max 1024 chars          # capability + trigger, no process steps
license: MIT
metadata:
  author: GitHub URL
  version: "1.0.0"
  domain: backend                    # language|backend|frontend|infrastructure|api-architecture|quality|devops|security|data-ml|platform|specialized|workflow
  triggers: keyword1, keyword2
  role: specialist                   # specialist|expert|architect|engineer
  scope: implementation              # implementation|review|design|system-design|testing|analysis|infrastructure|optimization|architecture
  output-format: code                # code|document|report|architecture|specification|schema|manifests|analysis|analysis-and-code|code+analysis
  related-skills: skill-dir-name
---
```
- `allowed-tools`: 공백 구분, 툴 제한 시에만 사용

**Reference files:** 100–220줄, 단일 토픽, 이디엄 패턴 우선. 전체 기준 → `docs/skills-authorship.md`

**Progressive disclosure:** SKILL.md = ~80-100줄 (역할·트리거·워크플로·제약·라우팅). Reference = 심화 내용 온디맨드 로딩.

## Project Workflow

**Creating:** 기존 overlap 확인 → SKILL.md 작성 → reference 생성 → 라우팅 테이블 추가 → 트리거 테스트 → SKILLS_GUIDE.md 업데이트

**Modifying:** 전체 스킬 읽기 → description 포맷 유지 → disclosure 구조 보존 → 크로스레퍼런스 업데이트 → 라우팅 테이블 검증

## Release

전체 7단계 → `docs/skills-release.md`

```bash
python scripts/update-docs.py        # 버전·카운트 업데이트
python scripts/validate-skills.py    # skills 유효성 검증
python scripts/validate-markdown.py  # 마크다운 문법 검증
```

## Attribution

[obra/superpowers](https://github.com/obra/superpowers) · License: MIT · Research: `research/superpowers.md`
