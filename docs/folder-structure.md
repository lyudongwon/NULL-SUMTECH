# 폴더 구조

> 2026-10-29 문서 통일 때 Next.js 한 프로젝트 기준으로 다시 썼다. 실제 뼈대는 `feat/base-settings` 브랜치에 있다. 앞서 정한 "도메인 우선 + 계층 분리"와 "화면 레이어드" 원칙은 그대로 Next.js 폴더에 옮겼다. 파일 주인은 [roles.md](roles.md)를 본다. 바뀌면 이 파일을 **그 변경과 같은 커밋에서** 고친다([ai-rules.md](ai-rules.md) 5번).

## 저장소 최상위

```text
NULL-SUMTECH/
├── app/                    화면(페이지)과 API(Route Handler)
├── components/             재사용 UI
├── lib/                    도메인 로직·DB·계약 타입
├── drizzle/                마이그레이션(drizzle-kit 생성)
├── e2e/                    Playwright (e2e/scenarios/<id>.spec.ts, e2e/fixtures/)
├── docs/                   팀 협업 문서
├── public/
├── instrumentation.ts      서버 시작 시 마이그레이션·시드
├── Dockerfile
├── docker-compose.yml
├── CLAUDE.md
├── README.md
└── progress.md             (저장소 루트에 둔다)
```

## API (서버)

도메인 우선이다. 도메인 이름은 [api.md](api.md)의 도메인 열과 같다. 계층은 Spring 방식의 controller·service·dto·entity·repository를 아래처럼 옮긴다.

| 계층 | 위치 | 하는 일 |
| --- | --- | --- |
| controller | `app/api/v1/<도메인>/.../route.ts` | 요청 검사(Zod), service 호출, 응답·에러 변환. 얇게 둔다 |
| service | `lib/<도메인>/` (예: `lib/judge/`) | 업무 규칙. 판정 규칙처럼 테스트할 로직은 여기 둔다 |
| dto | `lib/contracts/<도메인>.ts` | 요청·응답 타입과 Zod 스키마. 화면과 API가 같이 쓴다 |
| entity | `lib/db/schema.ts` | Drizzle 표 정의(①만 고친다) |
| repository | `lib/<도메인>/` 안의 DB 조회 함수 | Drizzle 쿼리. route.ts에서 DB를 직접 부르지 않는다 |

```text
app/api/v1/
├── users/                  POST 발급, [user_id]/restrictions GET·PATCH (REQ-4)  ①
├── restrictions/presets/   GET (REQ-4)  ①
├── restaurants/            GET recommendations (OPT-2)  ①
├── images/                 POST (REQ-1)  ②
├── menus/recognize/        POST (REQ-2)  ②
├── judgments/              POST, manual/ POST (REQ-3, REQ-6, OPT-1)  ③
└── ingredients/infer/      POST (REQ-5)  ③
lib/
├── contracts/              restriction.ts ① · image.ts ② · judgment.ts ③
├── db/                     schema.ts, index.ts, setup.ts, seed/  ①(seed/foods.ts는 ③)
├── judge/                  판정 규칙 R0~R4, 재료 찾기 순서, Vitest  ③
├── recipe-search.ts        만개의 레시피 검색  ③
├── ai.ts                   메뉴 판독 AI 호출  ②
├── image.ts                OPT-3 이미지 축소(브라우저)  ②
└── user-id.ts              user_id 발급·localStorage 보관  ①
```

### 규칙

| 항목 | 규칙 |
| --- | --- |
| 의존 방향 | `route.ts` → `lib/<도메인>` → `lib/db`. 반대로 부르지 않는다 |
| DB 노출 | Drizzle 행을 그대로 응답하지 않는다. `lib/contracts`의 형식(snake_case)으로 바꿔 돌려준다 |
| 도메인 간 호출 | 다른 도메인은 그 도메인의 `lib/<도메인>` 함수로만 부른다(예: 판정이 REQ-5 재료 찾기를 부름) |
| 판정과 AI | 판정 규칙은 `lib/judge/`의 순수 함수다. AI는 재료를 찾을 때만 부른다 |

## 화면

레이어드 방식이다. 위 계층이 아래 계층만 부른다.

```text
app/                    화면 계층: 라우트·레이아웃·페이지. 얇게 둔다
├── layout.tsx          공통 레이아웃 + 하단 고지 문구(REQ-7)  ①
├── page.tsx            첫 화면(서비스 이름·하는 일, "식단 제한 설정"·"사진 올리기")  ①
├── settings/           식단 제한 설정(REQ-4)  ①
├── scan/               사진 올리기·메뉴 이름 입력칸(REQ-1, OPT-1)  ②
├── result/             판정 화면·근거 상세·"다른 식당 보기"(REQ-3, REQ-6)  ②
└── restaurants/        음식점 추천(OPT-2)  ①
components/<화면>/      화면 계층: 재사용 UI
lib/                    데이터·유틸 계층: API 호출 함수, 이미지 축소, 계약 타입
```

| 항목 | 규칙 |
| --- | --- |
| API 호출 | `fetch`는 각 화면 폴더 또는 `lib/`의 호출 함수에서만 한다. 컴포넌트가 직접 부르지 않는다 |
| 이미지 축소 | OPT-3은 `lib/image.ts`의 순수 함수로 두고 업로드 직전에 부른다 |
| 타입 | `lib/contracts/`를 화면과 API가 같이 쓴다 |
