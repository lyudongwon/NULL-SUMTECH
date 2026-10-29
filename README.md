# NULL-SUMTECH

SUMTECH 2026 루프톤 NULL 팀의 **Can I Eat? (먹어도 돼?)** 저장소입니다.

학식 메뉴판에는 요리 이름만 적혀 있어서, 돼지고기를 못 먹는 이슬람 유학생이나 땅콩 알레르기가 있는 유학생은 고추장·양념장 속 재료를 알 수 없어 먹고 싶은 메뉴를 포기하곤 합니다. Can I Eat?은 메뉴판이나 음식 사진 한 장을 올리면, 미리 저장해 둔 식단 제한(알레르기·할랄·채식)에 맞춰 메뉴마다 안전 / 확인 필요 / 불가로 판정하고 어떤 재료 때문인지 근거를 보여 줍니다.

핵심 기능은 사진 업로드 → 메뉴 판독 → 숨은 성분 추론 → 3단계 판정과 근거이고, 화면은 영어로 제공합니다. 문제 정의와 기능 목록(REQ/OPT)은 [docs/problem.yaml](docs/problem.yaml)에 있습니다.

## 문서

어떤 문서를 언제 읽는지는 [CLAUDE.md](CLAUDE.md)의 문서 지도에 정리돼 있습니다. 기술 스택은 [docs/tech-stack.md](docs/tech-stack.md), 폴더 구조는 [docs/folder-structure.md](docs/folder-structure.md)를 보세요.

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

`http://localhost:3000`에서 열리고, 상태는 `http://localhost:3000/__health`로 확인합니다.

### 로컬 개발

PostgreSQL을 따로 띄운 뒤 백엔드와 화면을 각각 실행합니다.

```bash
cd backend && ./gradlew bootRun
```

```bash
cd frontend && npm install && npm run dev
```

`SPRING_DATASOURCE_*`가 없으면 백엔드는 `localhost:5432/foodcheck`(사용자·비밀번호 `foodcheck`)에 붙고, `BACKEND_URL`이 없으면 화면은 `http://localhost:8080`으로 요청을 넘깁니다.

### DB 표를 바꿀 때 (역할 ①)

`backend/src/main/resources/db/migration/`에 다음 번호로 `V<번호>__설명.sql`을 추가하세요. 이미 올라간 V 파일은 고치지 않고, 시드는 `R__` 파일에서 고칩니다.

## 테스트

| 종류 | 명령 | 내용 |
| --- | --- | --- |
| 백엔드 단위 | `cd backend && ./gradlew test` | JUnit 5 |
| 스모크 | `cd e2e && npm ci && npx playwright install chromium && npm run smoke` | 앱이 떠 있는 상태에서 health·API 프록시·첫 화면·식단 제한 저장·음식점 추천·빈 메뉴 팝업을 빠르게 본다 |
| 흐름 E2E | `cd e2e && npm test` | `docs/scenarios.yaml` 흐름 7개(`e2e/scenarios/<id>.spec.ts`). 테스트 사진은 `e2e/fixtures/` |

E2E는 앱을 먼저 띄운 뒤 돌립니다. 기본 주소는 `http://localhost:3000`이고, 다른 주소는 `BASE_URL`로 지정합니다. CI(`.github/workflows/ci.yml`)는 푸시·PR마다 백엔드 테스트, 화면 lint·타입·빌드를 돌리고, 빈 DB에서 `docker compose up` 후 2분 안에 `/__health`가 응답하는지와 스모크 테스트를 확인합니다.

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
