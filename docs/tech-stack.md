# 기술 스택

> 2026-10-29 노션 [기능 명세서](https://app.notion.com/p/408c4b3d7d9e8022b62ce4bec5b58225) 맨 아래 표를 옮긴 것이다. 노션이 원본이고 이 파일은 사본이다. 노션이 바뀌면 이 파일도 같은 커밋에서 고친다. 이 표를 팀이 확정한 것으로 보고 진행하되, 확정이 아니면 팀에 확인한다.

| 영역 | 선택 | 메모 |
| --- | --- | --- |
| 언어·런타임 | TypeScript + Node.js 22 | |
| 프레임워크 | Next.js 15 이상 (App Router) | 화면과 API를 한 프로젝트에서 만든다 |
| 화면 | React + Tailwind CSS | 모바일 웹 |
| DB | PostgreSQL + Drizzle ORM | 코드에서 마이그레이션을 실행할 수 있어서, 시작 명령을 서버 실행만으로 유지할 수 있다 |
| AI | Vercel AI SDK(`ai`) + AI Gateway | `AI_GATEWAY_API_KEY`를 자동으로 읽는다. `generateObject`로 정해진 형식의 결과를 받는다 |
| 검증 | Zod | AI 응답과 API 입력의 형식 검사 |
| 테스트 | Vitest(판정 엔진) + Playwright(E2E) | |
| 배포 | Vercel + Neon(무료 Postgres) | GitHub에 푸시하면 자동 배포된다. 서버가 잠들지 않는다 |

## 이 스택에서 지켜야 할 실행 조건

[progress.md](../progress.md)의 3단계(최소 화면 docker 실행·CI·첫 배포)에서 옮긴 것이다.

- 앱은 컨테이너 안 3000 포트에서 받는다.
- `GET /__health`는 인증·리다이렉트 없이 200을 준다. Next.js에서는 `app/%5F%5Fhealth`로 만든다.
- 빌드는 Dockerfile에서 끝내고 시작 명령은 서버 실행만 한다. Drizzle 마이그레이션을 코드에서 실행하는 이유가 이것이다.
- `docker compose up --build`만으로 빈 DB에서 앱과 시드 데이터가 2분 안에 떠야 한다.
- 환경변수 이름은 [README.md](../README.md)의 표를 따른다(`AI_GATEWAY_API_KEY` 등).

DB 구조는 [erd.md](erd.md)를 본다.
