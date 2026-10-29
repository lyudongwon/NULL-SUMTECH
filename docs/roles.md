# 역할 분배

> 화면은 영어다(REQ-8). 화면 문구는 [api.md](api.md) > 화면 문구를 그대로 쓴다.
>
> 2026-10-29 기준, ①의 뼈대·REQ-4·OPT-2가 `main`에 올라온 뒤 남은 일로 다시 나눴다. 세 사람이 각자 Claude 세션에서 동시에 작업해도 충돌하지 않도록 기능·API·파일을 나눈다. 기능은 [problem.yaml](problem.yaml)의 `features`(REQ-1~8, OPT-1~3), API는 [api.md](api.md), 기술 스택은 [tech-stack.md](tech-stack.md)(Spring Boot 백엔드 + Next.js 화면), 폴더는 [folder-structure.md](folder-structure.md)를 따른다. 아래 경로의 `<pkg>`는 `backend/src/main/java/com/nullteam/foodcheck`(잠정 기본 패키지)다.

## 역할

| 역할 | 맡는 일 | 담당 API (`/api/v1` 아래) | 한 줄 요약 |
| --- | --- | --- | --- |
| **① 기반·결과 화면** | 뼈대·인프라(완료), REQ-4 식단 제한 설정(완료), OPT-2 주변 음식점 지도(**다시 만든다**: 카카오맵 + 사용자 보고, V2 마이그레이션 포함), REQ-7 고지 문구(완료), **REQ-3·REQ-6 결과 화면** `/result`, 업로드 한도 10MB 설정, **E2E 흐름 7개 전부**, Docker 확인, CI, 배포 | `POST /users`, `GET /restrictions/presets`, `GET`·`PATCH /users/{user_id}/restrictions`, `GET /restaurants/nearby`, `PUT /restaurants/{id}/reports` | 판정 결과를 보여 주고, 앱 전체가 테스트·배포되게 한다 |
| **② 사진·판독** | REQ-1 사진 업로드(`/scan`), OPT-3 이미지 축소, REQ-2 메뉴 판독(Spring AI 비전), OPT-1 메뉴 이름 입력칸 | `POST /images`, `POST /menus/recognize` | 사진을 메뉴 이름 목록으로 바꾼다 |
| **③ 판정 엔진** | REQ-5 숨은 성분 추론(음식 DB → 이름·조리법 → 만개의 레시피 → AI 추정), REQ-3 판정 규칙 R0~R4, REQ-6 근거 데이터, OPT-1 판정 API, 음식·재료 시드 | `POST /ingredients/infer`, `POST /judgments`, `POST /judgments/manual` | 메뉴 이름을 판정과 근거로 바꾼다 |

- 흐름은 ② 사진 → 메뉴 이름, ③ 메뉴 이름 → 판정, ① 판정 → 화면·테스트·배포다. 세 역할이 만나는 곳은 아래 [만나는 지점](#만나는-지점) 두 군데뿐이다.
- 결과 화면은 처음에 ②가 맡았지만, ②에 사진 업로드·축소·AI 판독·입력칸이 몰려 있어 ①로 옮겼다. ①은 `/restaurants`와 공통 레이아웃을 이미 갖고 있어 "Other restaurants"까지 이어서 만든다.
- E2E 흐름 7개는 모두 "설정 → 사진 → 결과"를 지나가므로, 테스트 틀(Playwright 설정·공통 도우미)과 spec은 ① 한 사람이 만든다.
- 판정 규칙은 단위 테스트가 필요한 곳([ai-rules.md](ai-rules.md) 7번)이라 ③이 화면 없이 백엔드 로직과 데이터에 집중한다.

## 만나는 지점

먼저 고정하고, 바꿀 때는 이 문서와 [api.md](api.md)를 같은 커밋에서 고친다.

1. **② → ①: 화면 이동 주소.** 판독이 끝나거나 OPT-1 입력칸에 이름을 넣고 "Can I eat it?"를 누르면, ②가 `/result?menus=<JSON 배열>`로 이동시킨다. 예: `/result?menus=["설렁탕","콩나물국"]`(URL 인코딩). 결과 화면(①)이 이 목록과 `user_id`로 `POST /judgments`를 부른다.
2. **③ → ①: 판정 결과 타입.** ③이 첫 커밋으로 `frontend/src/types/judgment.ts`와 `frontend/src/services/judgmentService.ts`를 올린다. 형식은 [api.md](api.md) REQ-3 응답(`level`, `reason`, `matched_restrictions`, `rule`, `ingredients[]`의 `evidence`·`confidence`·`basis`·`restrictions`) 그대로다. ①은 판정 API가 나오기 전까지 이 타입으로 만든 예시 데이터로 결과 화면을 만든다.

## 파일 소유권

주인만 고친다. 다른 역할의 파일을 고쳐야 하면 주인에게 요청한다.

| 주인 | 백엔드 (`backend/`) | 화면 (`frontend/`) | 그 밖 |
| --- | --- | --- | --- |
| ① | `build.gradle`, `settings.gradle`, `gradle/`, `Dockerfile`, `src/main/resources/application.yml`, `<pkg>/FoodcheckApplication.java`, `<pkg>/global/`, `<pkg>/domain/user/`, `<pkg>/domain/restriction/`, `<pkg>/domain/restaurant/`, `src/test/java/.../domain/restriction/`, `db/migration/V*__*.sql`(표 변경 전부), `db/migration/R__10_seed_restrictions.sql`, `R__20_seed_restaurants.sql` | `package.json`, lock 파일, `next.config.ts`, `tsconfig.json`, `Dockerfile`, `src/app/layout.tsx`, `src/app/globals.css`, `src/app/page.tsx`, `src/app/settings/`, `src/app/restaurants/`, **`src/app/result/`**, `src/app/%5F%5Fhealth/`, `src/app/api/v1/[...path]/`(백엔드 프록시), `src/components/settings/`, `src/components/restaurants/`, **`src/components/result/`**, `src/hooks/useRestrictions.ts`, `src/hooks/useRecommendations.ts`, **`src/hooks/useJudgment.ts`**, `src/hooks/userIdentity.ts`(`ensureUserId`), `src/services/userService.ts`, `src/services/restrictionService.ts`, `src/services/restaurantService.ts`, `src/types/restriction.ts`, `src/types/restaurant.ts`, `src/lib/userId.ts`(브라우저에 보관한 `user_id`), `src/lib/apiClient.ts`(`apiRequest`·`ApiError`, 다른 역할도 가져다 쓴다) | `docker-compose.yml`, `.github/`, `README.md`, **`e2e/`**(`e2e/fixtures/` 빼고) |
| ② | `<pkg>/domain/image/`, `<pkg>/domain/menu/` | `src/app/scan/`, `src/components/scan/`, `src/hooks/`의 사진·판독용 파일(예: `useScan.ts`), `src/services/imageService.ts`, `src/services/menuService.ts`, `src/types/image.ts`, `src/types/menu.ts`, `src/lib/resizeImage.ts` | `e2e/fixtures/`(테스트 사진) |
| ③ | `<pkg>/domain/judgment/`, `<pkg>/domain/ingredient/`, `db/migration/R__30_seed_foods.sql`, `src/test/java/.../domain/judgment/`·`ingredient/` | `src/types/judgment.ts`, `src/services/judgmentService.ts` | |

## 충돌 방지 규칙

- **약속(타입)은 도메인 주인이 정한다.** 백엔드 `domain/<도메인>/dto/`와 화면 `src/types/<도메인>.ts`는 그 도메인 주인만 고친다. 판정 결과 타입(`types/judgment.ts`)은 ③이 정하고 ①이 가져다 쓴다.
- **의존성과 DB 표는 ①만 고친다.** `build.gradle`, `frontend/package.json`·lock 파일, `V*__*.sql`은 ①만 고친다. 라이브러리나 컬럼이 더 필요하면 ①에게 요청한다(예: ②의 사진 본문 저장 위치, ③의 레시피 검색 라이브러리).
- **시드는 반복 마이그레이션(`R__`) 파일로 나눈다.** 프리셋·제한 항목·음식점은 ①(`R__10`·`R__20`), 음식·재료는 ③(`R__30`)이 맡는다. 여러 번 실행돼도 결과가 같게 쓴다.
- **`api.md`는 섹션별로 주인이 있다.** 공통·열거형·REQ-4·OPT-2는 ①, REQ-1·REQ-2는 ②, REQ-3·OPT-1·REQ-5는 ③이 고친다. API를 바꾸면 코드와 같은 커밋에서 문서도 고치고, PR 맨 위에 "계약 변경"이라고 적는다([ai-rules.md](ai-rules.md) 5번).
- **에러 응답은 공통 형식을 쓴다.** 백엔드에서 `ApiException.badRequest/notFound/conflict("한국어 문장")`을 던지면 `global/exception`이 `{ "detail": "..." }`로 바꾼다.
- **사용자 제한은 서비스로 받는다.** ③은 `RestrictionService.findEnabledItemCodes(userId)`로 사용자가 켠 항목을 받는다(사용자가 없으면 empty, 저장한 적 없으면 빈 집합). `restriction` 도메인의 repository·entity를 직접 쓰지 않는다.
- **브랜치는 각자 따로 쓴다.** [git-convention.md](git-convention.md)대로 `feat/base-settings`(①), `feat/photo-scan`(②), `feat/judgment-engine`(③)을 쓰고 작게 자주 PR로 합친다. 시작 전마다 `main`을 받는다.
- **E2E는 ①이 만든다.** `e2e/scenarios/<id>.spec.ts`로 흐름마다 파일 하나다. 테스트 사진은 ②가 `e2e/fixtures/`에 넣는다. 판정 엔진 단위 테스트(JUnit 5)는 ③이 맡는다.
- **E2E 메뉴는 시드에 넣는다.** 예외는 `manual-input-unknown-menu`의 "된장찌개" 하나다. DB에 없는 경로를 확인하는 흐름이고, 만개의 레시피가 막혀도 AI 추정으로 배지가 나오게 해서 외부 사이트와 상관없이 통과시킨다.

## 작업 순서

### 0단계: 약속을 먼저 고정한다

1. **①(완료)** `backend/`·`frontend/` 뼈대, `docker-compose.yml`(app·backend·db), REQ-4 API와 `/settings`, OPT-2 API와 `/restaurants`, 첫 화면, 하단 고지 문구를 `main`에 올렸다.
2. **②·③** `main`을 받자마자 첫 커밋으로 자기 약속 파일만 올린다.
   - ③: `domain/judgment` dto, `frontend/src/types/judgment.ts`, `frontend/src/services/judgmentService.ts`. 형식은 [api.md](api.md) REQ-3 응답 그대로다.
   - ②: `domain/image`·`menu` dto, `types/image.ts`·`types/menu.ts`. 사진 본문 저장 위치([erd.md](erd.md) 열린 질문 1)를 정해 ①에게 표 변경을 요청한다.

### 1단계부터: 동시에 진행한다

- ①: 업로드 한도 10MB(`application.yml`) → `/result` 화면(예시 데이터로 시작) → `e2e/` Playwright 설정과 흐름 7개 spec → Docker 확인·CI → 배포 → 팀 조사 음식점 시드(`R__20`)
- ②: REQ-1 업로드(`/scan`, `POST /images`) → OPT-3 축소 → REQ-2 판독(Spring AI 비전) → OPT-1 입력칸 → `/result?menus=...`로 이동 → 테스트 사진(`e2e/fixtures/`: menu-board, seolleongtang, no-menu, bibimbap, bibimguksu)
- ③: 음식·재료 시드(`R__30`) → 판정 규칙 R0~R4와 JUnit → `POST /judgments` → `POST /judgments/manual` → REQ-5 재료 찾기(만개의 레시피 → AI 추정)

## 각 Claude 세션 시작 지시문

**① 기반·결과 화면**

> CLAUDE.md, docs/ai-rules.md, docs/git-convention.md, docs/roles.md를 먼저 읽어. 나는 역할 ①(기반·결과 화면)이야. feat/base-settings 브랜치에서 application.yml의 업로드 한도를 10MB로 먼저 올리고, /result 화면을 만들어: ③의 src/types/judgment.ts 기준으로 `/result?menus=<JSON 배열>`의 메뉴를 POST /api/v1/judgments로 판정해 배지·근거 한 줄·메뉴를 누르면 보이는 REQ-6 상세(확인/추정, 확신도, 적용 규칙)·이름이 비었을 때 "Please enter a menu name." 팝업·'안전'이 하나도 없을 때 "Other restaurants"(/restaurants)를 보여 줘. 판정 API가 나오기 전에는 예시 데이터로 개발해. '등록되지 않은 메뉴' 팝업은 만들지 않아. 그다음 e2e/에 Playwright 설정과 docs/scenarios.yaml 흐름 7개 spec을 만들고, Docker 확인·CI·배포를 준비해. 내 담당 파일만 고쳐.

**② 사진·판독**

> CLAUDE.md, docs/ai-rules.md, docs/git-convention.md, docs/roles.md를 먼저 읽어. 나는 역할 ②(사진·판독)야. feat/photo-scan 브랜치에서 첫 커밋으로 backend domain/image·menu의 dto와 frontend src/types/image.ts·menu.ts를 올리고, docs/api.md 기준으로 REQ-1(/scan 화면, 파일 선택·드래그·카메라, POST /api/v1/images), OPT-3(src/lib/resizeImage.ts, 긴 변 1400px, JPEG 0.85, 1400 미만은 그대로), REQ-2(POST /api/v1/menus/recognize, Spring AI 구조화 출력, 못 읽으면 비움)를 만들어. 메뉴를 하나도 못 읽거나 422·500이면 OPT-1 메뉴 이름 입력칸과 "Can I eat it?"를 보여 줘. 판독이 끝나거나 "Can I eat it?"를 누르면 `/result?menus=<JSON 배열>`로 이동시켜(결과 화면은 ①이 만든다). E2E용 테스트 사진은 e2e/fixtures/에 넣어. 내 담당 파일만 고치고, 필요한 라이브러리나 DB 표는 ①에게 요청해.

**③ 판정 엔진**

> CLAUDE.md, docs/ai-rules.md, docs/git-convention.md, docs/roles.md를 먼저 읽어. 나는 역할 ③(판정 엔진)이야. feat/judgment-engine 브랜치에서 첫 커밋으로 backend domain/judgment의 dto와 frontend src/types/judgment.ts·src/services/judgmentService.ts를 올려. 형식은 docs/api.md REQ-3 응답 그대로야(①이 결과 화면에서 쓴다). 그다음 db/migration/R__30_seed_foods.sql(erd.md 시드 최소 요구의 음식과 숨은 재료: 육수, 젓갈, 맛술, 햄, 어묵 등. 비빔밥 고추장의 다진 돼지고기, 비빔국수 양념장의 땅콩은 추정 재료로 넣어. 된장찌개는 넣지 마), 판정 규칙 R0~R4와 JUnit 테스트, POST /api/v1/judgments, POST /api/v1/judgments/manual, REQ-5 재료 찾기(POST /api/v1/ingredients/infer: 음식 DB → 이름·조리법 → 만개의 레시피 → AI 추정, 찾은 재료는 foods에 저장)를 만들어. 사용자 제한은 RestrictionService.findEnabledItemCodes(userId)로 받아. DB에 없는 메뉴에도 404를 주지 말고 판정해. 내 담당 파일만 고치고, 필요한 라이브러리나 DB 표는 ①에게 요청해.

## 팀 확인 필요

- 사용자 식별자 이름: `user_id`로 통일했다. 회원·로그인 없이 서버가 Auto Increment로 발급한다(`decisions.md`).
- 만개의 레시피 검색: 사이트 이용약관과 robots.txt를 확인한다. 막히면 AI 추정으로 넘어가므로 기능은 멈추지 않는다.
- 음식점 시드: 학교 주변 음식점을 조사해 `R__20_seed_restaurants.sql`에 넣는다(①). 지금은 "(예시)" 가짜 데이터다.
- 배포 대상: [tech-stack.md](tech-stack.md) 열린 결정 1(화면 Vercel, 백엔드 AWS 컨테이너 후보)과 DB 호스트(Neon 또는 AWS RDS)를 정해야 ①이 배포를 진행한다.
- 잠정값: `docker compose`의 `app`(Next.js), 기본 패키지 `com.nullteam.foodcheck`, Java·Spring Boot 버전([decisions.md](decisions.md)). 팀이 바꾸면 이 문서의 경로도 같이 고친다.
