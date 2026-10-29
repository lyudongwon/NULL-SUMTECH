# 기술 스택

> 2026-10-29 문서 통일 때 **Next.js 하나(화면 + API)**로 정했다. 앞서 정한 Spring Boot 분리안을 대신한다(이유는 [decisions.md](decisions.md)). 실제 구성은 `feat/base-settings` 브랜치의 뼈대와 같다. 작업하면서 바뀌면 이 파일과 요약한 곳(`CLAUDE.md`, `erd.md`, `README.md`)을 **그 변경과 같은 커밋에서** 고친다([ai-rules.md](ai-rules.md) 5번).

상태: `사용` = 쓰기로 했다. `미정` = 쓰기로 했지만 세부가 아직 안 정해졌다.

## 구성

화면과 API를 Next.js 한 프로젝트에 둔다. API는 Route Handler(`app/api/v1/...`)이고 계약은 [api.md](api.md)다.

| 영역 | 선택 | 상태 | 메모 |
| --- | --- | --- | --- |
| 언어·프레임워크 | TypeScript + Next.js 15.5 (App Router), React 19 | 사용 | `output: "standalone"`으로 빌드 |
| 화면 | Tailwind CSS 4 | 사용 | 모바일 웹(`scenarios.yaml` `platform: mobile`) |
| DB | PostgreSQL 16 | 사용 | 로컬·E2E는 `docker-compose.yml`의 `db`. 배포용 호스트는 `미정`(Neon 무료 등급 후보) |
| ORM·마이그레이션 | Drizzle ORM + drizzle-kit | 사용 | 스키마 `lib/db/schema.ts`, 마이그레이션 `drizzle/`. 서버 시작 시 `instrumentation.ts`가 마이그레이션과 시드를 실행한다 |
| AI | Vercel AI SDK(`ai`) + AI Gateway | 사용 | `AI_GATEWAY_API_KEY`를 읽는다. 메뉴 판독(REQ-2, 비전)과 숨은 성분 AI 추정(REQ-5)에만 쓴다. 판정 규칙은 AI가 아니라 코드다. 모델은 `미정` |
| 검증 | Zod | 사용 | API 입력과 AI 구조화 출력 검사 |
| 사진 저장 | Postgres `bytea`(`images.data`) | 사용 | 외부 저장소 없이 DB 하나로 끝낸다 |
| 만개의 레시피 검색 | 서버 `fetch` + HTML 파싱 | 미정 | 파서 라이브러리는 ③이 고른다. 실패하면 AI 추정으로 넘어간다(REQ-5) |
| 테스트 | Vitest(판정 규칙 단위 테스트) + Playwright(E2E) | 사용 | `npm test`, `npm run e2e` |
| 배포 | 공개 HTTPS URL 하나 | 미정 | 같은 Docker 이미지를 그대로 올릴 수 있는 곳(예: Render, Fly.io, Railway) 또는 Vercel + Neon |

## 실행 조건

[progress.md](../progress.md) 3단계(최소 화면 docker 실행·CI·첫 배포)의 조건이고, 이 구성은 그대로 만족한다.

- `docker compose`의 `app`(Next.js)은 컨테이너 안 3000 포트에서 받는다. `app`만 `ports`를 갖고 `env_file`은 쓰지 않는다.
- `GET /__health`는 인증·리다이렉트 없이 200을 준다(`app/%5F%5Fhealth/route.ts`). `RELEASE_ID`를 돌려준다.
- 빌드는 Dockerfile에서 끝내고 시작 명령은 `node server.js`만 한다.
- `docker compose up --build`만으로 빈 DB에서 앱과 시드 데이터가 2분 안에 떠야 한다.
- 환경변수 이름은 [README.md](../README.md)의 표를 따른다(`DATABASE_URL`, `RELEASE_ID`, `AI_GATEWAY_API_KEY`).

## 열린 결정

1. **배포 대상.** 위 "배포" 행. 정하면 CI(`.github/`)에 자동 배포를 붙인다.
2. **AI 호출 남용 방지.** 로그인이 없고 팀 AI 예산이 $10이라, `user_id`·IP당 분당 호출 수 제한을 둔다. 방식은 ②·③이 AI 호출을 만들 때 정한다.
3. **E2E에서 AI 응답.** 실제 모델을 부르고 결과를 기다려 확인한다([progress.md](../progress.md) 6단계: AI 기능은 실제 응답). 기대 결과는 판정이 흔들려도 통과하도록 "세 배지 중 하나"처럼 쓰고, 고정 판정이 필요한 메뉴(설렁탕)는 시드 재료로 R1이 나오게 한다.

DB 구조는 [erd.md](erd.md)를 본다.
