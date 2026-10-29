# 역할 분배

> 2026-10-29 기준. 세 사람이 각자 Claude 세션에서 동시에 작업해도 충돌하지 않도록 기능·API·파일을 나눈다. 기능은 [problem.yaml](problem.yaml)의 `features`(REQ-1~7, OPT-1~3), API는 [api.md](api.md)를 따른다. 기술 스택(Next.js App Router + Drizzle + PostgreSQL)은 팀 확정 전 **(제안)** 이다.

## 역할

| 역할 | 맡는 기능 | 담당 API | 한 줄 요약 |
| --- | --- | --- | --- |
| **① 기반·설정** | 뼈대·인프라, REQ-4 식단 제한 설정, REQ-7 고지 문구, OPT-2 음식점 추천, 첫 화면 | `GET /restrictions/presets`, `GET`·`PATCH /users/{user_id}/restrictions`, `GET /restaurants/recommendations` | 앱이 뜨고 배포되며, 사용자 설정이 저장된다 |
| **② 사진·화면** | REQ-1 사진 업로드, OPT-3 이미지 축소, REQ-2 메뉴 판독(AI), OPT-1 입력 화면, REQ-3·REQ-6 결과 화면(배지·근거·상세·popup) | `POST /images`, `POST /menus/recognize` | 사진에서 판정 결과 화면까지, 사용자가 보는 흐름 전체 |
| **③ 판정 엔진** | REQ-5 숨은 성분 추론(음식 DB + 만개의 레시피), REQ-3 판정 로직, REQ-6 근거 데이터, OPT-1 판정 API | `POST /ingredients/infer`, `POST /judgments`, `POST /judgments/manual` | 메뉴 이름을 받아 판정과 근거를 돌려주는 서버 로직과 데이터 |

- 시나리오 흐름이 모두 "사진 → 판정 화면"이라 화면은 ②가 끝까지 맡아 이음새 충돌을 없앤다.
- 판정 규칙은 단위 테스트가 필요한 곳([ai-rules.md](ai-rules.md) 7번)이라 ③이 화면 없이 서버 로직과 데이터에 집중한다.
- REQ-7 고지 문구는 공통 레이아웃(`app/layout.tsx`)에 들어가므로 레이아웃 주인인 ①이 맡는다.

## 파일 소유권

주인만 고친다. 다른 역할의 파일을 고쳐야 하면 주인에게 요청한다.

| 주인 | 파일 |
| --- | --- |
| ① | `package.json`, lock 파일, `next.config.ts`, `app/layout.tsx`, `app/globals.css`, `app/page.tsx`, `app/settings/`, `app/restaurants/`, `app/api/v1/restrictions/`, `app/api/v1/users/`, `app/api/v1/restaurants/`, `app/%5F%5Fhealth/`, `lib/contracts/restriction.ts`, `lib/db/schema.ts`, `lib/db/index.ts`, `drizzle/`, `instrumentation.ts`, `lib/db/seed/index.ts`, `lib/db/seed/presets.ts`, `lib/db/seed/restaurants.ts`, `lib/user-id.ts`, `Dockerfile`, `docker-compose.yml`, `.github/`, `README.md` |
| ② | `app/scan/`, `app/result/`, `app/api/v1/images/`, `app/api/v1/menus/`, `components/scan/`, `components/result/`, `lib/contracts/image.ts`, `lib/image.ts`, `lib/ai.ts`, `e2e/fixtures/` |
| ③ | `app/api/v1/judgments/`, `app/api/v1/ingredients/`, `lib/contracts/judgment.ts`, `lib/judge/`(판정 규칙과 Vitest 테스트), `lib/recipe-search.ts`, `lib/db/seed/foods.ts` |

## 충돌 방지 규칙

- **약속(타입) 파일은 역할마다 따로 둔다.** `lib/contracts/`의 `restriction.ts`(①)·`image.ts`(②)·`judgment.ts`(③)는 각자 자기 파일만 고친다.
- **의존성과 DB 표는 ①만 고친다.** `package.json`·lock 파일·`lib/db/schema.ts`는 0단계에서 전부 만들어 두고, 더 필요하면 ①에게 요청한다.
- **시드 데이터는 파일로 나눈다.** 음식·재료는 ③(`foods.ts`), 프리셋·음식점은 ①(`presets.ts`·`restaurants.ts`)이 맡는다.
- **`api.md`는 섹션별로 주인이 있다.** 공통·열거형은 ①, REQ-1·REQ-2는 ②, REQ-3·OPT-1·숨은 성분 추론은 ③이 고친다. API를 바꾸면 코드와 같은 커밋에서 문서도 고치고, PR 맨 위에 "계약 변경"이라고 적는다([ai-rules.md](ai-rules.md) 5번).
- **브랜치는 각자 따로 쓴다.** [git-convention.md](git-convention.md)대로 `feat/base-settings`(①), `feat/photo-scan`(②), `feat/judgment-engine`(③)을 쓰고 작게 자주 PR로 합친다. 시작 전마다 `main`을 받는다.
- **E2E는 흐름마다 파일 하나다.** `e2e/scenarios/<id>.spec.ts`로 나눈다. `scan-menu-board`·`scan-food-photo-single-dish`·`manual-input-fallback`은 ②, `diet-toggle-only`는 ①이 맡는다. 판정 엔진 단위 테스트(Vitest)는 ③이 맡는다.

## 작업 순서

### 0단계: 약속을 먼저 고정한다

1. **①** Next.js 뼈대, 의존성 전부(`ai`, `zod`, `drizzle-orm`, `pg`, `drizzle-kit`, `vitest`, `@playwright/test`), DB 표(`users`, `user_restrictions`, `presets`, `images`, `foods`, `ingredients`, `food_ingredients`, `restaurants`), 마이그레이션 + 시드 실행기, `GET /__health`, `Dockerfile`, `docker-compose.yml`, 빈 `lib/contracts/` 폴더를 만들어 `main`에 올린다. 기본 경로를 `/api/v1`로 할지 정해 [decisions.md](decisions.md)에 남긴다([api.md](api.md) 열린 질문 1번).
2. **②·③** 뼈대를 받자마자 첫 커밋으로 자기 contracts 파일만 올린다.
   - ③ `judgment.ts`: REQ-6 상세에 필요한 필드(성분별 확인/추정, 확신도, 적용 규칙)를 정하고 `api.md`의 REQ-3 응답을 같은 커밋에서 고친다([api.md](api.md) 열린 질문 3·4번).
   - ② `image.ts`: 업로드와 판독 응답 형식을 정한다.

### 1단계부터: 동시에 진행한다

- ①: REQ-4 API 3개와 `/settings` 화면 → `app/layout.tsx` 하단 고지 문구(REQ-7) → 첫 화면 → CI·배포 → OPT-2 추천 API와 화면
- ②: REQ-1 업로드와 `/scan` 화면 → OPT-3 축소 → REQ-2 판독 → OPT-1 입력칸 → `/result` 화면(③의 API가 나오기 전에는 contracts 타입으로 만든 예시 JSON 사용)
- ③: 음식·재료 시드 → 판정 규칙과 Vitest → `POST /judgments` → `POST /judgments/manual` → REQ-5 추론과 만개의 레시피 검색

## 각 Claude 세션 시작 지시문

**① 기반·설정**

> CLAUDE.md, docs/ai-rules.md, docs/git-convention.md, docs/roles.md를 먼저 읽어. 나는 역할 ①(기반·설정)이야. feat/base-settings 브랜치에서 roles.md 0단계의 ① 작업(뼈대, 의존성, DB 표, 마이그레이션 + 시드 실행기, GET /__health, Dockerfile, docker-compose.yml, 빈 lib/contracts/)을 먼저 끝내고 main에 올려. 그다음 docs/api.md의 REQ-4 API 3개와 /settings 화면, app/layout.tsx 하단 고지 문구(REQ-7), 첫 화면, OPT-2 추천 API와 화면을 만들어. 내 담당 파일만 고쳐.

**② 사진·화면**

> CLAUDE.md, docs/ai-rules.md, docs/git-convention.md, docs/roles.md를 먼저 읽어. 나는 역할 ②(사진·화면)야. feat/photo-scan 브랜치에서 첫 커밋으로 lib/contracts/image.ts를 올리고, docs/api.md 기준으로 REQ-1(/scan 화면, 파일 선택·드래그·카메라, POST /images), OPT-3(긴 변 1400px, JPEG 0.85, 1400 미만은 그대로), REQ-2(POST /menus/recognize, AI SDK generateObject, 못 읽으면 비움)를 만들어. 판독이 비면 OPT-1 입력칸을 보여 줘. /result 화면은 ③의 lib/contracts/judgment.ts 타입으로 배지·근거·REQ-6 상세·popup(이름이 null이면 '입력 값이 없습니다.', 등록되지 않은 메뉴)을 만들고, 판정 API가 나오기 전에는 예시 JSON으로 개발해. 내 담당 파일만 고치고, 필요한 패키지나 DB 표는 ①에게 요청해.

**③ 판정 엔진**

> CLAUDE.md, docs/ai-rules.md, docs/git-convention.md, docs/roles.md를 먼저 읽어. 나는 역할 ③(판정 엔진)이야. feat/judgment-engine 브랜치에서 첫 커밋으로 lib/contracts/judgment.ts를 올려. REQ-6에 필요한 성분별 확인/추정, 확신도, 적용 규칙 필드를 넣고 docs/api.md의 REQ-3 응답도 같은 커밋에서 고쳐. 그다음 lib/db/seed/foods.ts(학식 메뉴와 숨은 재료: 육수, 젓갈, 맛술, 햄, 어묵 등), lib/judge/ 판정 규칙과 Vitest 테스트, POST /judgments, POST /judgments/manual, REQ-5 숨은 성분 추론(POST /ingredients/infer, DB에 없으면 lib/recipe-search.ts로 만개의 레시피 검색)을 만들어. api.md의 숨은 성분 추론 섹션 ID를 OPT-4에서 REQ-5로 바꿔. 내 담당 파일만 고치고, 필요한 패키지나 DB 표는 ①에게 요청해.

## 팀 확인 필요

- 사용자 식별자 이름: `user_id`로 통일했다. 회원·로그인 없이 서버가 Auto Increment로 발급한다(`decisions.md`).
- 만개의 레시피 검색: 사이트 이용약관과 robots.txt를 확인한다. 검색 결과는 DB에 저장해 같은 메뉴를 다시 검색하지 않는다. E2E에 쓰는 메뉴는 시드에 넣어 외부 사이트 없이도 테스트가 돌게 한다.
- 프리셋 이름: `scenarios.yaml`은 "채식"·"할랄", `api.md`의 프리셋 코드는 `HALAL`·`VEGAN`이다. "채식"이 `VEGAN`에 해당하는지 맞춘다.
- 기술 스택 확정: 확정되면 README 설치·실행 섹션과 `CLAUDE.md > 기술 스택`에 적는다.
