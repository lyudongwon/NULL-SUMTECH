# NULL-SUMTECH

SUMTECH 2026 루프톤 NULL 팀 서비스 저장소. 학식당 메뉴판·음식 사진 한 장으로 내 식단 제한에 맞는지 안전 / 확인 필요 / 불가로 알려 주는 웹 서비스.

## 문서

문서 목록과 언제 읽는지는 [CLAUDE.md](CLAUDE.md)의 문서 지도를 본다.

## 기술 구성 (제안, 팀 확정 전)

- Next.js 15 (App Router, TypeScript) + Tailwind CSS
- PostgreSQL + Drizzle ORM. 마이그레이션은 `drizzle/`, 시드는 `lib/db/seed/`
- 서버가 시작될 때 `instrumentation.ts`가 마이그레이션과 시드를 실행한다(여러 번 실행돼도 결과가 같다)
- 테스트: Vitest(`npm test`), Playwright(`npm run e2e`)

## 설치·실행

### Docker (빈 DB에서 한 번에)

```bash
docker compose up --build
```

`http://localhost:3000`에서 열린다. 상태 확인은 `http://localhost:3000/__health`.

### 로컬 개발

PostgreSQL을 따로 띄우고 `.env`에 `DATABASE_URL`을 넣은 뒤:

```bash
npm install
npm run dev
```

### DB 표를 바꿀 때 (역할 ①)

`lib/db/schema.ts`를 고친 뒤 마이그레이션 파일을 만들어 함께 커밋한다.

```bash
npm run db:generate
```

## 환경변수

| 이름 | 용도 | 설정 위치 |
| --- | --- | --- |
| `DATABASE_URL` | PostgreSQL 접속 주소 | 로컬 `.env`, 배포 환경변수, `docker-compose.yml` |
| `RELEASE_ID` | `/__health`가 돌려주는 배포 commit | 배포 환경변수(CI에서 commit SHA), `docker-compose.yml` |
| `AI_GATEWAY_API_KEY` | 서비스 AI 기능 호출(필요할 때만) | 로컬 `.env`, 배포 환경변수, GitHub Secrets |
