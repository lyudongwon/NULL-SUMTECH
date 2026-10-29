# API 명세

> 노션 [기능 명세서 > 기능리스트](https://app.notion.com/p/408c4b3d7d9e80c59094ec4995ab33fb)를 2026-10-29에 옮겨 와 문서 통일 때 손봤다. API는 이 파일이 원본이라, 바꿀 일이 생기면 여기부터 고치고 노션 통합 페이지는 따라 옮긴다.
> 기능 ID는 [problem.yaml](problem.yaml)의 `features`를 따르고, 통일하며 정한 값은 [decisions.md](decisions.md)에 있다.

## 공통

| 항목 | 값 |
| --- | --- |
| 기본 경로 | 모든 API는 `/api/v1`로 시작한다. 예외: `GET /__health`(대회 실행 조건) |
| 서버 | Spring Boot 백엔드(`backend/`의 `domain/<도메인>/controller`). 브라우저는 같은 출처 `/api/v1/**`을 부르고 Next.js(`frontend/src/app/api/v1/[...path]`)가 백엔드로 넘긴다. [tech-stack.md](tech-stack.md) |
| 인증 | 없다. 로그인 없이 쓰고(`scenarios.yaml` `login: false`), 서버가 Auto Increment로 발급한 `user_id`(숫자)로 구분한다. 처음 접속하면 `POST /api/v1/users`로 받아 브라우저 `localStorage`에 보관한다 |
| 요청 형식 | `application/json`. 사진 업로드(REQ-1)만 `multipart/form-data` |
| 응답 필드 | snake_case |
| 화면 언어 | 영어(REQ-8). 사용자에게 보이는 값(프리셋·항목 `name`, `reason`, `detail`)은 영어로 돌려준다. 메뉴·재료 이름(`menu_name`·`ingredients[].name`)은 메뉴판에 적힌 한국어 원문 그대로 준다. 화면 문구는 아래 [화면 문구](#화면-문구-req-8) |
| 에러 본문 | 모든 에러는 `{ "detail": "<사람이 읽는 영어 문장>" }`. 화면은 `detail`을 그대로 팝업에 보여 줄 수 있다 |
| 서버 오류 | 모든 엔드포인트에서 서버 내부 오류는 500 Internal Server Error |

### 엔드포인트 목록

| 기능 ID | 기능 | 도메인 | 메서드 | 경로 | 담당 | 연관 화면 |
| --- | --- | --- | --- | --- | --- | --- |
| REQ-4 | 사용자 번호 발급 | user | POST | `/api/v1/users` | ① | 첫 화면 |
| REQ-4 | 식단 제한 프리셋 목록 조회 | restriction | GET | `/api/v1/restrictions/presets` | ① | 식단 제한 설정 |
| REQ-4 | 내 식단 제한 조회 | restriction | GET | `/api/v1/users/{user_id}/restrictions` | ① | 식단 제한 설정 |
| REQ-4 | 식단 제한 저장 | restriction | PATCH | `/api/v1/users/{user_id}/restrictions` | ① | 식단 제한 설정 |
| REQ-1 | 사진 업로드 | image | POST | `/api/v1/images` | ② | 사진 올리기 |
| REQ-2 | 메뉴 판독 | menu | POST | `/api/v1/menus/recognize` | ② | 판정 화면 |
| REQ-3, REQ-6 | 위험 정도 3단계 판정 + 근거 상세 | judgment | POST | `/api/v1/judgments` | ③ | 판정 화면 |
| OPT-1 | 수동 입력 판정 | judgment | POST | `/api/v1/judgments/manual` | ③ | 메뉴 이름 입력칸 |
| REQ-5 | 숨은 성분 추론 | ingredient | POST | `/api/v1/ingredients/infer` | ③ | 판정 화면(내부 호출) |
| OPT-2 | 음식점 추천 | restaurant | GET | `/api/v1/restaurants/recommendations` | ① | Other restaurants |
| OPT-3 | 이미지 축소 | client | — | 없음 (브라우저 처리) | ② | 사진 올리기 |
| REQ-7 | 고지 문구 | client | — | 없음 (공통 레이아웃) | ① | 모든 화면 |

### 열거형

| 필드 | 쓰이는 곳 | 값 |
| --- | --- | --- |
| `level` | REQ-3, OPT-1 | `SAFE`(안전, 화면 "Safe") · `CHECK`(확인 필요, 화면 "Check first") · `BLOCKED`(불가, 화면 "Avoid") |
| `image_type` | REQ-2 | `MENU_BOARD`(메뉴판, 메뉴 전부) · `FOOD`(음식 사진, `menus` 1개) |
| `source` (사진) | REQ-1 | `FILE` · `DRAG_DROP` · `CAMERA` |
| `preset` | REQ-4 | `HALAL`(할랄: 돼지고기·술) · `VEGETARIAN`(채식: 고기·해산물 제외, 계란·유제품 허용) · `VEGAN`(비건: 고기·해산물·계란·유제품 제외) |
| 제한 항목 `code` | REQ-4, REQ-3, OPT-2 | `meat`(고기) · `pork`(돼지고기) · `beef`(소고기) · `seafood`(해산물) · `egg`(계란) · `dairy`(우유·유제품) · `peanut`(땅콩) · `alcohol`(술) |
| `evidence` | REQ-5, REQ-6 | `CONFIRMED`(확인: 음식 DB 시드에 확인된 재료로 등록된 재료, 화면 "Confirmed") · `INFERRED`(추정, 화면 "Estimated") |
| `confidence` | REQ-5, REQ-6 | `HIGH`(높음, 화면 "High") · `MEDIUM`(보통, 화면 "Medium") · `LOW`(낮음, 화면 "Low") |
| `basis` | REQ-5, REQ-6 | `DB`(음식 DB) · `NAME_KEYWORD`(이름 속 재료 단어) · `COOKING_METHOD`(조리법에서 추정) · `RECIPE_SEARCH`(만개의 레시피) · `AI_ESTIMATE`(AI 추정) |
| `rule` | REQ-3, REQ-6 | 아래 [판정 규칙](#판정-규칙)의 `R0`~`R4` |

- 재료 하나는 걸리는 제한 항목 전부에 연결한다(`ingredient_restriction_items`). 돼지고기는 `pork`와 `meat`, 소고기는 `beef`와 `meat`에 걸린다. 그래서 "고기"를 켜도, "돼지고기"만 켜도 돼지고기가 걸린다.
- 알레르기는 프리셋 없이 `egg`·`dairy`·`peanut`·`seafood` 토글로 고른다.

### 화면 문구 (REQ-8)

화면은 영어다. 시나리오(`scenarios.yaml`)와 E2E는 아래 영어 문구를 그대로 쓴다. 문구를 바꾸면 이 표와 `scenarios.yaml`을 같은 커밋에서 고친다.

| 쓰이는 곳 | 뜻 | 화면 문구 |
| --- | --- | --- |
| 첫 화면 | 서비스 이름 | Can I Eat? |
| 첫 화면 | 하는 일 | Snap a cafeteria menu or dish, and see if it fits your diet: Safe, Check first, or Avoid. |
| 첫 화면·버튼 | 식단 제한 설정 | Diet settings |
| 첫 화면·버튼 | 사진 올리기 | Upload photo |
| 설정 화면 | 프리셋 | Halal · Vegetarian · Vegan |
| 설정 화면 | 토글 | Meat · Pork · Beef · Seafood · Egg · Dairy · Peanut · Alcohol |
| 설정 화면·버튼 | 저장 | Save |
| 설정 화면 | 안내 | Pick a preset or turn foods on and off, then save. |
| 설정 화면 | 묶음 제목 | Presets · Foods I can't eat |
| 설정 화면·버튼 | 저장 중 | Saving… |
| 설정 화면 | 서버가 이유를 주지 않은 오류 · 다시 시도 버튼 | Couldn't load your settings. · Couldn't save your settings. · Try again |
| 설정 화면(REQ-4) | 동시 저장 충돌(409 `detail`) | Another request was saved first. Please try again. |
| 사진 올리기 화면(REQ-1) | 안내 | Upload one photo of a menu board or a dish. |
| 사진 올리기 화면(REQ-1) | 끌어 놓기 안내(넓은 화면에서만) | You can also drag a photo here. |
| 사진 올리기 화면·버튼(REQ-1) | 카메라로 찍기 · 앨범에서 고르기 | Take a photo · Choose from album |
| 사진 올리기 화면(REQ-1, 화면 읽기 이름) | 사진 파일 입력 | Photo file |
| 카메라 화면(REQ-1, 화면 읽기 이름) | 카메라 창 · 미리보기 · 방금 찍은 사진 | Camera · Camera preview · Photo you just took |
| 카메라 화면(REQ-1) | 카메라 켜는 중 | Opening camera… |
| 카메라 화면·버튼(REQ-1) | 찍기 · 닫기 | Take photo · Cancel |
| 카메라 화면·버튼(REQ-1) | 다시 찍기 · 이 사진 쓰기 | Retake · Use photo |
| 사진 올리기 화면(REQ-1·REQ-2) | 진행 상태 | Uploading photo… · Reading the menu… · Opening results… |
| 사진 올리기 화면(REQ-1·REQ-2) | 서버가 이유를 주지 않은 오류 | Couldn't upload the photo. · Couldn't read the menu. |
| 입력칸(OPT-1) | 메뉴 이름 입력칸 | Menu name |
| 입력칸(OPT-1)·버튼 | 판정하기 | Can I eat it? |
| 판정 화면 | 배지 | Safe · Check first · Avoid |
| 판정 화면·링크 | 다시 찍기 | Retake |
| 판정 화면 | 서버가 이유를 주지 않은 오류 | Couldn't load the results. |
| 근거 상세(REQ-6) | 확인/추정 | Confirmed · Estimated |
| 근거 상세(REQ-6) | 확신도 | High · Medium · Low |
| 근거 상세(REQ-6) | 적용 규칙 | Rule |
| 근거 상세(REQ-6) | 재료 표의 열 이름 | Ingredient · Evidence · Confidence · Source |
| 근거 상세(REQ-6) | 재료 출처(`basis`) | Food database · Menu name · Cooking method · 10000recipe · AI estimate |
| 근거 상세(REQ-6) | 적용 규칙 설명(R0~R4 순서) | No diet restrictions set · A confirmed ingredient is restricted · An estimated ingredient may be restricted · No ingredients from the food database or 10000recipe · No restricted ingredients found |
| 첫 화면·판정 화면·버튼(OPT-2) | 다른 식당 보기 | Other restaurants |
| 모든 화면·링크 | 처음으로 | Home |
| 음식점 추천(OPT-2) | 결과 없음 | No restaurants match your diet yet. |
| 음식점 추천(OPT-2) | 안내 | Restaurants that fit your diet, for when the cafeteria doesn't. |
| 음식점 추천(OPT-2) | 식단 제한을 저장하기 전 | Please set your diet first. |
| 음식점 추천(OPT-2) | 음식점이 피하는 항목(뒤에 항목 이름이 붙음) | Free of |
| 음식점 추천(OPT-2) | 서버가 이유를 주지 않은 오류 | Couldn't load recommendations. |
| 팝업(REQ-3·OPT-1) | 메뉴 이름이 비었음 | Please enter a menu name. |
| 팝업·버튼 | 팝업 닫기 | OK |
| 설정·음식점 추천 화면 | 불러오는 중 | Loading… |
| 모든 화면 하단(REQ-7) | 고지 문구 | For guidance only. Please make the final check yourself or with a dietitian. |

- 메뉴·재료 이름은 메뉴판에 적힌 한국어 원문 그대로 보인다(예: `비빔밥`, `고추장`). 학식당의 한국어 메뉴판과 대조하고, 직원에게 그대로 보여 줄 수 있어야 하기 때문이다. 판정 근거 문장은 영어이고, 그 안에 재료 이름이 한국어 원문으로 들어간다(예: `May contain 고추장 속 다진 돼지고기. Check for Pork before eating.`).
- 한국어 화면은 만들지 않는다.

---

## REQ-4 사용자 번호 발급

`POST /api/v1/users` — 회원 정보 없이 사용자 번호를 하나 발급한다. 클라이언트는 `localStorage`에 `user_id`가 없을 때만 부른다.

### 요청

바디 없다.

### 응답 201 Created

```json
{
  "user_id": 1,
  "created_at": "2026-10-29T12:00:00+09:00"
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| 서버 내부 오류 | 500 Internal Server Error |

---

## REQ-4 식단 제한 프리셋 목록 조회

`GET /api/v1/restrictions/presets` — 설정 화면에 보여줄 프리셋과 개별 토글 항목 목록을 조회한다.

### 요청

헤더·바디 없다.

### 응답 200 OK

```json
{
  "presets": [
    { "code": "HALAL", "name": "Halal", "items": ["pork", "alcohol"] },
    { "code": "VEGETARIAN", "name": "Vegetarian", "items": ["meat", "seafood"] },
    { "code": "VEGAN", "name": "Vegan", "items": ["meat", "seafood", "egg", "dairy"] }
  ],
  "items": [
    { "code": "meat", "name": "Meat" },
    { "code": "pork", "name": "Pork" },
    { "code": "beef", "name": "Beef" },
    { "code": "seafood", "name": "Seafood" },
    { "code": "egg", "name": "Egg" },
    { "code": "dairy", "name": "Dairy" },
    { "code": "peanut", "name": "Peanut" },
    { "code": "alcohol", "name": "Alcohol" }
  ]
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| 서버 내부 오류 | 500 Internal Server Error |

---

## REQ-4 내 식단 제한 조회

`GET /api/v1/users/{user_id}/restrictions` — 사용자가 저장한 프리셋과 토글 상태를 불러온다.

### 요청

| 위치 | key | 설명 |
| --- | --- | --- |
| Path | `user_id` | `POST /api/v1/users`로 받은 번호 |

### 응답 200 OK

```json
{
  "user_id": 1,
  "preset": "HALAL",
  "toggles": { "pork": true, "alcohol": true, "peanut": false },
  "version": 3,
  "updated_at": "2026-10-29T12:00:00+09:00"
}
```

### 에러

404 Not Found

```json
{
  "detail": "No saved diet settings."
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| 발급되지 않았거나 아직 한 번도 저장하지 않은 `user_id` | 404 Not Found → 클라이언트는 기본값(제한 없음) 표시 |
| 서버 내부 오류 | 500 Internal Server Error |

---

## REQ-4 식단 제한 저장

`PATCH /api/v1/users/{user_id}/restrictions` — 프리셋 선택과 토글로 바뀐 제한 음식 설정을 저장한다. 처음 저장하면 행을 만든다.

### 요청

바디

```json
{
  "preset": "HALAL",
  "toggles": { "peanut": true },
  "version": 3
}
```

- `preset`: 키를 보내지 않으면 프리셋을 바꾸지 않는다. 프리셋 코드를 보내면 모든 토글을 끄고 그 프리셋의 항목을 켠 뒤 `toggles`를 덮어쓴다. `null`이면 프리셋 선택만 지우고 토글은 그대로 둔다
- `toggles`: 바뀐 항목만 보내도 된다(on-click 단위)
- `version`: 마지막으로 조회한 버전. 처음 저장할 때는 생략한다. 이미 저장한 적이 있는데 생략하면 409

### 응답 200 OK

```json
{
  "user_id": 1,
  "preset": "HALAL",
  "toggles": { "pork": true, "alcohol": true, "peanut": true },
  "version": 4,
  "updated_at": "2026-10-29T12:01:00+09:00"
}
```

### 에러

409 Conflict

```json
{
  "detail": "Another request was saved first. Please try again."
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| 같은 사용자의 저장이 동시에 들어옴(`version` 불일치, 처음 저장이 겹침) | 409 Conflict → 클라이언트는 다시 조회한 뒤 재요청 |
| 존재하지 않는 preset / 토글 코드 | 400 Bad Request |
| 발급되지 않은 `user_id` | 404 Not Found `{ "detail": "User not found." }` → 클라이언트는 번호를 다시 발급받는다 |
| 서버 내부 오류 | 500 Internal Server Error |

---

## REQ-1 사진 업로드

`POST /api/v1/images` — 파일 선택, 드래그 앤 드롭, 모바일 카메라 촬영으로 고른 사진을 서버에 올리고 `image_id`를 받는다. 사진 본문을 어디에 둘지는 ②가 정한다([erd.md](erd.md) 열린 질문 1).

### 요청

바디 (form-data)

| key | type | 필수 | 설명 |
| --- | --- | --- | --- |
| `file` | File | O | JPEG / PNG / WEBP 이미지 (브라우저에서 OPT-3 축소 후 전송), 최대 10MB |
| `source` | Text | X | `FILE` · `DRAG_DROP` · `CAMERA` |

### 응답 201 Created

```json
{
  "image_id": "img_8f3a2c",
  "width": 1400,
  "height": 1050,
  "uploaded_at": "2026-10-29T12:00:00+09:00"
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `file` 누락 | 400 Bad Request `{ "detail": "Please choose a photo to upload." }` |
| 이미지가 아닌 파일 / 지원하지 않는 형식 | 415 Unsupported Media Type `{ "detail": "This image format is not supported." }` |
| 10MB 초과 | 413 Payload Too Large `{ "detail": "The photo is too large (max 10MB)." }` |
| 잘못된 `source` 값 | 400 Bad Request `{ "detail": "Invalid source value." }` |
| 서버 내부 오류 | 500 Internal Server Error |

### 사진 본문 저장 (②가 정함, 2026-10-29)

- 형식은 브라우저가 보낸 Content-Type이 아니라 파일 앞부분(JPEG·PNG·WEBP 서명)으로 판단하고, `width`·`height`도 서버가 사진에서 읽는다.
- 메타데이터는 `images` 표에 두고, 본문은 우선 백엔드 메모리(최대 200장, 30분)에 둔다. 판독(REQ-2)은 업로드 직후 한 번만 하므로 오래 둘 필요가 없다. 서버를 다시 켜면 본문이 사라져 그 뒤의 판독 요청은 404다.
- 목표는 Postgres `bytea`다. ①에게 `images.data bytea` 컬럼(V 마이그레이션)을 요청했고, 추가되면 DB에 저장하도록 바꾼다. S3는 쓰지 않는다(키·버킷 설정이 늘고, 보관 기간이 짧다).

---

## REQ-2 메뉴 판독

`POST /api/v1/menus/recognize` — 메뉴판 사진이면 메뉴 이름을 전부, 음식 사진이면 요리명 1개를 뽑는다. AI 비전 모델을 쓴다.

### 요청

```json
{
  "image_id": "img_8f3a2c"
}
```

### 응답 200 OK

```json
{
  "image_id": "img_8f3a2c",
  "image_type": "MENU_BOARD",
  "menus": [
    { "index": 0, "name": "제육볶음" },
    { "index": 1, "name": null },
    { "index": 2, "name": "미역국" }
  ]
}
```

- 읽지 못한 항목은 추측하지 않고 `name: null`로 비워 둔다
- `name`은 메뉴판에 적힌 한국어 원문이다(REQ-8)
- 클라이언트는 `name`이 null이 아닌 항목만 모아 REQ-3에 보낸다
- 모든 `name`이 null이거나 `menus`가 비었으면 판정 화면 대신 메뉴 이름 입력칸(OPT-1)을 보여 준다

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `image_id` 누락 | 400 Bad Request |
| 존재하지 않는 `image_id` | 404 Not Found `{ "detail": "Image not found." }` |
| 메뉴판·음식 사진으로 인식되지 않음 | 422 Unprocessable Entity `{ "detail": "This doesn't look like a menu or food photo." }` → 클라이언트는 메뉴 이름 입력칸(OPT-1)을 보여 준다 |
| 일부 메뉴를 읽지 못함 | 200 OK, 해당 항목 `name: null` |
| 판독 엔진(AI) 오류·시간 초과(45초) | 500 Internal Server Error `{ "detail": "We couldn't read the menu. Please type the menu name." }` → 클라이언트는 메뉴 이름 입력칸(OPT-1)을 보여 준다 |

### 판독 방식 (②가 정함, 2026-10-29)

- Spring AI 구조화 출력으로 `kind`(`MENU_BOARD`·`FOOD`·`NOT_MENU`)와 메뉴 이름 목록을 받는다. `NOT_MENU`는 422다. 이름의 앞뒤 공백은 지우고, 빈 이름은 `null`로 바꾼다. 음식 사진은 첫 이름 1개만, 메뉴판은 최대 30개까지 돌려준다.
- 모델은 환경변수 `MENU_AI_MODEL`로 바꾼다. 기본값은 `openai/gpt-5-mini`다(잠정, ②가 정함. 코드: `SpringAiMenuReader`).
- E2E에서 판독 응답을 고정하지 않는다. 실제 모델을 부르고, 테스트 사진(`e2e/fixtures/`)은 글자가 크고 또렷하게 만들어 흔들림을 줄인다([tech-stack.md](tech-stack.md) 열린 결정 6).
- 화면은 판독이 끝나면 `name`이 있는 항목만 순서대로(중복 제외) 모아 `/result?menus=<JSON 배열>`로 이동한다([roles.md](roles.md) 만나는 지점 1).

---

## REQ-3 위험 정도 3단계 판정 (REQ-6 근거 상세 포함)

`POST /api/v1/judgments` — 메뉴마다 사용자 식단 제한 기준으로 안전 / 확인 필요 / 불가 중 하나의 배지, 근거 한 줄, 근거 상세(REQ-6)를 돌려준다. 재료는 내부에서 REQ-5 추론으로 찾는다.

### 요청

```json
{
  "user_id": 1,
  "menu_names": ["제육볶음", "미역국"]
}
```

- `menu_names`는 1개 이상이고, 각 값은 비어 있지 않은 문자열이어야 한다

### 응답 200 OK

```json
{
  "results": [
    {
      "menu_name": "제육볶음",
      "level": "BLOCKED",
      "reason": "Contains 돼지고기, which is not allowed for your Pork restriction.",
      "matched_restrictions": ["pork"],
      "rule": "R1",
      "ingredients": [
        { "name": "돼지고기", "evidence": "CONFIRMED", "confidence": "HIGH", "basis": "DB", "restrictions": ["pork", "meat"] },
        { "name": "고추장", "evidence": "CONFIRMED", "confidence": "HIGH", "basis": "DB", "restrictions": [] },
        { "name": "양파", "evidence": "CONFIRMED", "confidence": "HIGH", "basis": "DB", "restrictions": [] }
      ]
    },
    {
      "menu_name": "미역국",
      "level": "CHECK",
      "reason": "May contain 맛술. Check for Alcohol before eating.",
      "matched_restrictions": [],
      "rule": "R2",
      "ingredients": [
        { "name": "미역", "evidence": "CONFIRMED", "confidence": "HIGH", "basis": "DB", "restrictions": [] },
        { "name": "소고기", "evidence": "CONFIRMED", "confidence": "HIGH", "basis": "DB", "restrictions": ["beef", "meat"] },
        { "name": "맛술", "evidence": "INFERRED", "confidence": "MEDIUM", "basis": "COOKING_METHOD", "restrictions": ["alcohol"] }
      ]
    }
  ]
}
```

- `reason`: 근거 한 줄, 영어(배지 옆에 보인다)
- `menu_name`·`ingredients[].name`은 한국어 원문이다(REQ-8)
- `matched_restrictions`: 확인된(`CONFIRMED`) 재료 때문에 걸린 제한 항목. `BLOCKED`의 근거다
- `rule`: 적용된 판정 규칙(REQ-6)
- `ingredients[].restrictions`: 그 재료가 연결된 제한 항목 코드 전부(사용자와 상관없이 재료에 정해진 값). 어느 항목에도 걸리지 않으면 `[]`
- 예시는 할랄(`pork`·`alcohol`) 사용자 기준이다

### 판정 규칙

사용자가 켠 제한 항목과 재료의 `restrictions`를 맞대어 위에서부터 처음 맞는 규칙을 쓴다.

| 규칙 | 조건 | `level` |
| --- | --- | --- |
| `R0` | 사용자 제한이 하나도 없다(미설정·미등록 `user_id` 포함) | `SAFE` |
| `R1` | `CONFIRMED` 재료가 제한에 걸린다 | `BLOCKED` |
| `R2` | `INFERRED` 재료가 제한에 걸린다 | `CHECK` |
| `R3` | 걸리는 재료는 없지만 음식 DB(`DB`)나 만개의 레시피(`RECIPE_SEARCH`)에서 온 재료가 하나도 없다(재료가 AI 추정·이름 단어·조리법 추정뿐이거나 비었다) | `CHECK` |
| `R4` | 걸리는 재료가 없다 | `SAFE` |

- AI 추정(`AI_ESTIMATE`) 재료는 항상 `INFERRED`다. 그래서 AI 추정만으로는 `BLOCKED`가 나오지 않고, 제한이 있는 사용자에게는 걸리면 `R2`, 걸리지 않으면 `R3`로 늘 `CHECK`가 나온다(REQ-5).
- REQ-5는 AI 추정 결과에도 이름 단어(`NAME_KEYWORD`)·조리법(`COOKING_METHOD`) 추정을 덧붙인다. 그래서 `R3`는 "모두 `AI_ESTIMATE`"가 아니라 "`DB`·`RECIPE_SEARCH` 재료가 없음"으로 본다. 그렇지 않으면 AI 추정에 조리법 재료 하나만 섞여도 `R4`(`SAFE`)가 나와 위 줄과 어긋난다.
- 재료를 하나도 찾지 못해 `ingredients`가 비어도 `R3`(`CHECK`)이다. 제한이 없는 사용자는 그보다 먼저 `R0`(`SAFE`)이 된다.
- `R0`·`R3`의 `reason`은 응답 예시에 없어 지금 구현(`JudgmentRules`) 문구를 적는다. `R0`: `You have no diet restrictions set.` / `R3`(재료 없음): `No ingredients were found. Please check before eating.` / `R3`(DB·레시피 재료 없음): `Ingredients are only estimated, not from the food database or 10000recipe. Please check before eating.`
- 판정은 코드(`backend` `domain/judgment/service`)가 한다. AI는 재료를 찾을 때만 쓴다. 규칙마다 JUnit 테스트를 둔다.

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `menu_names`가 비었거나 빈 값·null을 포함 | 400 Bad Request `{ "detail": "Please enter a menu name." }` → 클라이언트는 같은 문구로 팝업 |
| 미등록 `user_id`(식단 제한 미설정) | `R0`로 판정, 200 OK |
| 음식 DB에 없는 메뉴 | REQ-5 순서(만개의 레시피 → AI 추정)로 재료를 찾아 판정, 200 OK |
| 서버 내부 오류 | 500 Internal Server Error |

---

## OPT-1 수동 입력 판정

`POST /api/v1/judgments/manual` — 판독에 실패하면 메뉴 이름을 직접 입력받아 REQ-3과 같은 엔진으로 판정한다.

### 요청

```json
{
  "user_id": 1,
  "menu_name": "된장찌개"
}
```

### 응답 200 OK

REQ-3 `results`의 한 항목과 같은 형식이다. 예시는 음식 DB에 없어 만개의 레시피에서 재료를 찾은 경우, 할랄 사용자 기준이다.

```json
{
  "menu_name": "된장찌개",
  "level": "SAFE",
  "reason": "No ingredients conflict with your diet restrictions.",
  "matched_restrictions": [],
  "rule": "R4",
  "ingredients": [
    { "name": "된장", "evidence": "INFERRED", "confidence": "HIGH", "basis": "RECIPE_SEARCH", "restrictions": [] },
    { "name": "멸치 육수", "evidence": "INFERRED", "confidence": "MEDIUM", "basis": "RECIPE_SEARCH", "restrictions": ["seafood"] }
  ]
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `menu_name` 누락 또는 빈 문자열 | 400 Bad Request `{ "detail": "Please enter a menu name." }` → 클라이언트 팝업 |
| 음식 DB에 없는 메뉴 | REQ-5 순서로 재료를 찾아 판정, 200 OK (팝업 없음) |
| 서버 내부 오류 | 500 Internal Server Error |

---

## REQ-5 숨은 성분 추론

`POST /api/v1/ingredients/infer` — 음식 이름으로 재료를 찾는다. REQ-3·OPT-1이 내부에서 같은 함수를 부르고, 이 API는 그 함수를 직접 확인할 때 쓴다.

### 찾는 순서

1. 음식 DB(`foods`): 등록된 재료는 `CONFIRMED`, `basis: DB`. 시드에 함께 넣은 숨은 재료(육수, 젓갈, 맛술 등)는 `INFERRED`, `basis: COOKING_METHOD`
2. 이름 속 재료 단어·조리법: 이름의 재료 단어(예: "돈" → 돼지고기)는 `NAME_KEYWORD`, 조리법(예: "튀김" → 밀가루·계란)은 `COOKING_METHOD`. 둘 다 `INFERRED`
3. 만개의 레시피 검색(1에 없을 때): 찾은 재료는 `INFERRED`, `basis: RECIPE_SEARCH`
4. AI 추정(3이 실패했거나 결과가 없을 때): `INFERRED`, `basis: AI_ESTIMATE`, `confidence`는 최대 `MEDIUM`

3·4에서 찾은 재료는 `foods`(`source`가 `RECIPE_SEARCH` 또는 `AI_ESTIMATE`)에 저장해 두고, 다음 요청부터는 1에서 바로 찾는다.

### 요청

```json
{
  "menu_name": "돈가스"
}
```

### 응답 200 OK

```json
{
  "menu_name": "돈가스",
  "ingredients": [
    { "name": "돼지고기", "evidence": "INFERRED", "confidence": "HIGH", "basis": "NAME_KEYWORD", "restrictions": ["pork", "meat"] },
    { "name": "밀가루", "evidence": "INFERRED", "confidence": "HIGH", "basis": "COOKING_METHOD", "restrictions": [] },
    { "name": "계란", "evidence": "INFERRED", "confidence": "MEDIUM", "basis": "COOKING_METHOD", "restrictions": ["egg"] }
  ]
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `menu_name` 누락 또는 빈 문자열 | 400 Bad Request `{ "detail": "Please enter a menu name." }` |
| 1~4 모두 재료를 찾지 못함 | 200 OK, `ingredients: []` → 판정은 `R3`(`CHECK`). 제한이 없는 사용자는 `R0`(`SAFE`) |
| 서버 내부 오류 | 500 Internal Server Error |

---

## OPT-2 음식점 추천

`GET /api/v1/restaurants/recommendations` — 첫 화면의 "Other restaurants"와, 판정 결과에 '안전' 메뉴가 하나도 없을 때 판정 화면에 나타나는 "Other restaurants"에서 부른다. 사용자가 켠 제한 항목을 모두 피하는 음식점을 돌려준다.

### 요청

| 위치 | key | 필수 | 설명 |
| --- | --- | --- | --- |
| Query | `user_id` | O | 사용자 번호 |
| Query | `limit` | X | 추천 개수 (기본 5, 1~20. 범위 밖 값은 가까운 끝값으로 맞춘다. 코드: `RestaurantService.MAX_LIMIT`) |

```text
GET /api/v1/restaurants/recommendations?user_id=1&limit=5
```

### 응답 200 OK

```json
{
  "restaurants": [
    {
      "restaurant_id": 12,
      "name": "OO 할랄 식당",
      "category": "중동식",
      "address": "...",
      "matched_restrictions": ["alcohol", "pork"]
    }
  ]
}
```

- `matched_restrictions`: 이 음식점이 피하는 항목 전부(`restaurant_restriction_items`, `code` 사전순). 사용자 값을 되돌려 주지 않고 음식점 데이터에서 꺼내므로, 추천된 음식점에는 사용자가 켠 항목이 모두 들어 있다. 화면은 이 값을 "Free of …"로 보여 준다
- 음식점 데이터는 시드(`R__20_seed_restaurants.sql`)다. 목표는 팀이 조사한 학교 주변 음식점이지만, 출처가 정해지지 않아(`erd.md` 열린 질문 7) 지금은 이름에 "(예시)"를 붙인 예시 데이터 5곳만 있다
- 추천 순서는 음식점 번호 순이다(기준 미정, `RestaurantService`의 `TODO(확인)`)

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `user_id` 누락 | 400 Bad Request |
| `user_id`가 존재하지 않음 | 404 Not Found `{ "detail": "User not found." }` |
| 조건에 맞는 음식점 없음 | 200 OK, `restaurants: []` → 화면에 "No restaurants match your diet yet." |
| 서버 내부 오류 | 500 Internal Server Error |

---

## OPT-3 이미지 축소 (브라우저 처리)

별도 API가 없다. REQ-1 `POST /api/v1/images`를 호출하기 직전에 브라우저에서 처리한다.

| 항목 | 값 |
| --- | --- |
| 입력 | 원본 이미지 |
| 리사이즈 기준 | 긴 변 1400px (비율 유지) |
| 출력 형식 | JPEG, 품질 0.85 |
| 출력 | REQ-1 `file` 필드로 전송 |
| 긴 변이 1400px 미만 | 축소하지 않고 원본 그대로 전송 |

---

## 열린 질문

1. `user_id` 보호: 로그인이 없어서 `user_id`만 알면 남의 식단 제한을 보고 고칠 수 있다. 해커톤에서는 이대로 간다([decisions.md](decisions.md)).
2. AI 호출 남용: 로그인이 없는데 팀 AI 예산은 $10이라 호출 제한이 필요하다. 방식은 [tech-stack.md](tech-stack.md) 열린 결정 4에서 다룬다.
3. 메뉴 이름 표기 차이: 판독한 이름과 DB 이름의 띄어쓰기·동의어(`돈까스`/`돈가스`)를 어떻게 맞출지는 ③이 정한다([erd.md](erd.md) 열린 질문 5).
4. 사진 본문 저장 위치: `images`에는 메타데이터만 있다. ②가 잠정 방식을 정했고(위 REQ-1 > 사진 본문 저장), ①에게 요청한 `images.data bytea` 컬럼은 아직 없다([erd.md](erd.md) 열린 질문 1).
5. 민감정보 고지: 식단 제한(알레르기·종교)은 `user_id`와 함께 서버 DB에 남고, 사진은 판독을 위해 외부 AI(AI Gateway)로 간다([tech-stack.md](tech-stack.md)). 화면에는 REQ-7 고지("For guidance only. …")만 있고 저장·외부 전송을 알리는 문구는 없다. 문구를 넣으면 위 화면 문구 표에 먼저 적는다(progress.md > 팀 결정 필요 > 위험 완화).
6. 설정 삭제: 저장한 식단 제한을 지우는 API·화면이 없다. `features`에 없는 기능이라 지금은 만들지 않는다(progress.md > 팀 결정 필요 > 위험 완화).
7. `R4`(`SAFE`)의 근거 범위: 음식 DB에 없는 메뉴는 만개의 레시피 재료로 판정한다. 일반 레시피는 학식당 레시피와 다를 수 있어, 걸리는 재료가 없으면 `SAFE`가 나와도 학식당 실제 재료와 다를 수 있다. 근거 상세에는 `Estimated`·`10000recipe`로 보인다(progress.md > 팀 결정 필요 > C, REQ-3·REQ-5).
