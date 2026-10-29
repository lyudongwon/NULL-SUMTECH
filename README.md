# NULL-SUMTECH

SUMTECH 2026 루프톤 NULL 팀 서비스 **Can I Eat? (먹어도 돼?)** 저장소. 학식당 메뉴판·음식 사진 한 장으로 내 식단 제한에 맞는지 안전 / 확인 필요 / 불가로 알려 주는 웹 서비스.

## 문서

문서 목록과 언제 읽는지는 [CLAUDE.md](CLAUDE.md)의 문서 지도를 본다. 기술 스택은 [docs/tech-stack.md](docs/tech-stack.md), 폴더는 [docs/folder-structure.md](docs/folder-structure.md)다.

## 구성

| 폴더 | 내용 | 포트 |
| --- | --- | --- |
| `frontend/` | Next.js 15 화면. `docker compose`의 `app`. `/api/v1/**`은 백엔드로 넘긴다 | 3000 |
| `backend/` | Spring Boot 4.1 (Java 21) API. 시작할 때 Flyway가 표와 시드를 만든다 | 8080 (내부) |
| `db` | PostgreSQL 16 | 내부 |

## 설치·실행

### Docker (빈 DB에서 한 번에)

```bash
docker compose up --build
```

`http://localhost:3000`에서 열린다. 상태 확인은 `http://localhost:3000/__health`.

### 로컬 개발

PostgreSQL을 따로 띄운 뒤 백엔드와 화면을 각각 실행한다.

```bash
cd backend && ./gradlew bootRun
```

```bash
cd frontend && npm install && npm run dev
```

백엔드는 `SPRING_DATASOURCE_*`가 없으면 `localhost:5432/foodcheck`(사용자·비밀번호 `foodcheck`)에 붙는다. 화면은 `BACKEND_URL`이 없으면 `http://localhost:8080`으로 넘긴다.

### DB 표를 바꿀 때 (역할 ①)

`backend/src/main/resources/db/migration/`에 다음 번호의 `V<번호>__설명.sql`을 추가한다. 이미 올라간 V 파일은 고치지 않는다. 시드는 `R__` 파일을 고친다.

## 테스트

| 종류 | 명령 | 내용 |
| --- | --- | --- |
| 백엔드 단위 | `cd backend && ./gradlew test` | JUnit 5 |
| 스모크 | `cd e2e && npm ci && npx playwright install chromium && npm run smoke` | 앱이 떠 있는 상태에서 health·API 프록시·첫 화면·식단 제한 저장·음식점 추천·빈 메뉴 팝업을 빠르게 본다 |
| 흐름 E2E | `cd e2e && npm test` | `docs/scenarios.yaml` 흐름 7개(`e2e/scenarios/<id>.spec.ts`). 테스트 사진은 `e2e/fixtures/` |

E2E는 앱을 따로 띄운 뒤 돌린다(기본 `http://localhost:3000`, 다른 주소는 `BASE_URL`). CI(`.github/workflows/ci.yml`)는 푸시·PR마다 백엔드 테스트, 화면 lint·타입·빌드, 빈 DB에서 `docker compose up` 후 2분 안의 `/__health`와 스모크 테스트를 돌린다.

## 환경변수

| 이름 | 쓰는 곳 | 용도 | 설정 위치 |
| --- | --- | --- | --- |
| `BACKEND_URL` | 화면 | `/api/v1/**`을 넘길 백엔드 주소 | 배포 환경변수, `docker-compose.yml` |
| `RELEASE_ID` | 화면·백엔드 | `/__health`가 돌려주는 배포 commit | 배포 환경변수(CI에서 commit SHA), `docker-compose.yml` |
| `SPRING_DATASOURCE_URL` | 백엔드 | PostgreSQL 접속 주소(`jdbc:postgresql://...`) | 로컬 `.env`, 배포 환경변수, `docker-compose.yml` |
| `SPRING_DATASOURCE_USERNAME` | 백엔드 | DB 사용자 | 같은 곳 |
| `SPRING_DATASOURCE_PASSWORD` | 백엔드 | DB 비밀번호 | 같은 곳 |
| `AI_GATEWAY_API_KEY` | 백엔드 | 서비스 AI 기능 호출(필요할 때만) | 로컬 `.env`, 배포 환경변수, GitHub Secrets |
| `AI_GATEWAY_BASE_URL` | 백엔드 | AI Gateway 주소. `/v1`까지 적는다(기본값 `https://ai-gateway.vercel.sh/v1`, 바꿀 때만) | 배포 환경변수 |
