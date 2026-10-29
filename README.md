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

## 구현 설계

기능 ID는 [docs/problem.yaml](docs/problem.yaml) `features`, API·판정 규칙은 [docs/api.md](docs/api.md)가 원본이다. 여기는 흩어진 결정을 한눈에 모은 요약이다.

### 핵심 흐름

1. 사진을 올린다(REQ-1). 화면 안 카메라·파일 선택·끌어 놓기 중 하나이고, 긴 변 1400px 이상이면 브라우저에서 줄여 보낸다(OPT-3).
2. 메뉴판이면 메뉴 이름 전부, 음식 사진이면 요리명 1개를 읽는다(REQ-2). 못 읽으면 메뉴 이름을 직접 넣는다(OPT-1).
3. 메뉴마다 숨은 재료를 찾는다(REQ-5): 음식 DB → 이름 속 재료 단어·조리법 → 만개의 레시피 → AI 추정.
4. 저장한 식단 제한(REQ-4)으로 정해진 규칙 R0~R4가 Safe / Check first / Avoid를 정하고(REQ-3), 메뉴를 누르면 재료별 근거가 보인다(REQ-6). AI는 판독과 재료 추정에만 쓰고 판정은 하지 않는다.
5. Safe 메뉴가 없으면 "Other restaurants"에서 주변 음식점 지도를 연다(OPT-2). 음식점 이름·주소·좌표는 카카오맵에서 가져오고, 핑은 다른 사용자의 보고로 미확인(Not checked)·안전(Looks fine)·주의(Heads up)를 표시한다.

### 저장하는 정보

| 무엇 | 어디에 | 얼마나 |
| --- | --- | --- |
| 사용자 번호(`user_id`) | 서버 DB(번호·생성 시각만), 브라우저 `localStorage` | 지울 방법이 아직 없다 |
| 식단 제한(할랄·알레르기·채식 항목) | 서버 DB, `user_id`와 함께 | 지울 방법이 아직 없다 |
| 사진 본문 | 백엔드 메모리, `user_id`와 잇지 않음 | 최대 200장·30분 |
| 찾아온 재료 | 음식 DB(`foods`) | 같은 메뉴를 다시 검색하지 않도록 계속 둔다 |
| 주변 음식점·사용자 보고 | 서버 DB(`restaurants`, `restaurant_reports`) | 계속 쌓는다 |
| 판독·판정 결과 | 저장하지 않는다 | — |

### 실패할 때

| 상황 | 동작 |
| --- | --- |
| 형식이 다르거나 10MB를 넘는 사진 | 올리지 않고 이유를 영어로 알린다 |
| 메뉴를 못 읽음, 메뉴판·음식 사진이 아님, 판독 45초 초과 | 메뉴 이름 입력칸(OPT-1)이 나온다 |
| 만개의 레시피 검색 실패 | AI가 재료를 추정한다. AI 추정만으로는 Avoid가 나오지 않는다 |
| 어디서도 재료를 못 찾음 | 재료 목록이 비고, 제한이 있으면 Check first(R3) |
| 식단 제한을 저장하지 않음 | 모든 메뉴가 Safe(R0)이고 근거에 제한이 없다고 적힌다 |
| 위치 권한 거부 | 조선대 주변 기본 좌표로 지도를 보여 주고 "Showing spots near Chosun University."를 보인다 |
| 카카오맵 호출 실패 | 이미 쌓인 음식점만 보여 준다. 지도는 비지 않는다 |
| 같은 사용자의 저장이 겹침 | 먼저 온 것만 저장하고 "Another request was saved first. Please try again."을 보인다 |

### 이번에 하지 않는 것 (정해진 것)

- 회원가입·로그인, 프로필 전환. 서버가 발급한 `user_id` 하나로 구분한다.
- 한국어 화면, 메뉴·재료 이름의 영어 번역. 화면은 영어이고 메뉴·재료는 메뉴판의 한국어 원문 그대로 보인다(메뉴판과 대조하고 직원에게 보여 주기 위해).
- 학식당 레시피 확보. 레시피를 모르는 메뉴는 Avoid라고 단정하지 않고 Check first까지만 말한다.
- '등록되지 않은 메뉴' 팝업. DB에 없는 메뉴도 위 순서로 재료를 찾아 판정한다.

### 아직 정하지 않은 것·확인하지 못한 가정

- 식판처럼 음식 사진 한 장에 요리가 여러 개일 때(지금은 요리명 1개만 읽는다).
- 저장한 식단 제한을 지우는 방법, 사진 보관 기간 원칙, 식단 제한·사진을 서버와 외부 AI에 보낸다는 고지.
- 만개의 레시피의 일반 레시피가 학식당 레시피와 같다는 가정. 그래서 재료가 추정(Estimated)일 때는 화면에 그렇게 표시한다.
- OPT-2 지도 방식(2026-10-30 계약 변경)은 문서만 바뀌었고, 카카오 호출·지도·보고 화면 코드는 아직이다. 카카오 API는 호출해 보지 못했고 조선대 기본 좌표도 확인 전이다. 보고가 쌓이기 전에는 대부분의 핑이 미확인으로 보인다.
- 성공 지표의 지금 값은 한 사람이 3회씩 잰 확인 시간과 두 사람의 기억에 기댄 포기 횟수다(`problem.yaml > canvas > impact` 주석).

팀 결정이 필요한 항목의 자세한 내용은 [progress.md](progress.md) `기획 검토 > 팀 결정 필요`에 있다.

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
| 흐름 E2E | `cd e2e && npm test` | `docs/scenarios.yaml` 흐름 8개(`e2e/scenarios/<id>.spec.ts`). 테스트 사진은 `e2e/fixtures/` |

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
| `KAKAO_REST_API_KEY` | 백엔드 | OPT-2 주변 음식점을 카카오 로컬 API에서 가져올 때 쓰는 REST 키 | 로컬 `.env`, 배포 환경변수, GitHub Secrets |
| `KAKAO_MAP_APP_KEY` | 화면 | 카카오맵 JavaScript SDK 키. 화면이 서버에서 읽어 브라우저로 내려 준다(`NEXT_PUBLIC_`을 쓰지 않는다 — 빌드 시점에 박히면 런타임에 바꿀 수 없다). 브라우저에 노출되므로 비밀값이 아니고, 콘솔에 플랫폼 도메인을 등록해야 한다 | 로컬 `.env`, 배포 환경변수, `docker-compose.yml` |
| `NEARBY_DEFAULT_LAT` | 백엔드 | 위치 권한을 거부했거나 시연할 때 쓰는 기본 위도(조선대 주변, `TODO(확인)`) | 로컬 `.env`, 배포 환경변수, `docker-compose.yml` |
| `NEARBY_DEFAULT_LNG` | 백엔드 | 같은 용도의 기본 경도 | 같은 곳 |
| `NEARBY_RADIUS_M` | 백엔드 | 주변 음식점 검색 반경(m). 기본 1000 | 같은 곳 |
