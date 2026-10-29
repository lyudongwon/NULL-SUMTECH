# API 명세

> 2026-10-29 노션 [기능 명세서 > 기능리스트](https://app.notion.com/p/408c4b3d7d9e80c59094ec4995ab33fb)를 옮긴 뒤 문서 통일 때 고쳤다. **이 파일이 API의 원본이다.** 노션 통합 페이지는 이 파일을 옮겨 적은 것이고, 바꿀 때는 이 파일을 먼저 고친다.
> 기능 ID는 [problem.yaml](problem.yaml)의 `features`를 따른다. 통일하면서 정한 값은 [decisions.md](decisions.md)에 있다.

## 공통

| 항목 | 값 |
| --- | --- |
| 기본 경로 | 모든 API는 `/api/v1`로 시작한다. 예외: `GET /__health`(대회 실행 조건) |
| 서버 | Next.js Route Handler(`app/api/v1/<도메인>/.../route.ts`). [tech-stack.md](tech-stack.md) |
| 인증 | 없다. 로그인 없이 쓰고(`scenarios.yaml` `login: false`), 서버가 Auto Increment로 발급한 `user_id`(숫자)로 구분한다. 처음 접속하면 `POST /api/v1/users`로 받아 브라우저 `localStorage`에 보관한다 |
| 요청 형식 | `application/json`. 사진 업로드(REQ-1)만 `multipart/form-data` |
| 응답 필드 | snake_case |
| 에러 본문 | 모든 에러는 `{ "detail": "<사람이 읽는 한국어 문장>" }` |
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
| OPT-2 | 음식점 추천 | restaurant | GET | `/api/v1/restaurants/recommendations` | ① | 다른 식당 보기 |
| OPT-3 | 이미지 축소 | client | — | 없음 (브라우저 처리) | ② | 사진 올리기 |
| REQ-7 | 고지 문구 | client | — | 없음 (공통 레이아웃) | ① | 모든 화면 |

### 열거형

| 필드 | 쓰이는 곳 | 값 |
| --- | --- | --- |
| `level` | REQ-3, OPT-1 | `SAFE`(안전) · `CHECK`(확인 필요) · `BLOCKED`(불가) |
| `image_type` | REQ-2 | `MENU_BOARD`(메뉴판, 메뉴 전부) · `FOOD`(음식 사진, `menus` 1개) |
| `source` (사진) | REQ-1 | `FILE` · `DRAG_DROP` · `CAMERA` |
| `preset` | REQ-4 | `HALAL`(할랄) · `VEGETARIAN`(채식: 고기·해산물 제외, 계란·유제품 허용) |
| 제한 항목 `code` | REQ-4, REQ-3, OPT-2 | `meat`(고기) · `pork`(돼지고기) · `beef`(소고기) · `seafood`(해산물) · `egg`(계란) · `dairy`(우유·유제품) · `peanut`(땅콩) · `alcohol`(술) |
| `evidence` | REQ-5, REQ-6 | `CONFIRMED`(확인: 음식 DB에 등록된 재료) · `INFERRED`(추정) |
| `confidence` | REQ-5, REQ-6 | `high`(높음) · `medium`(보통) · `low`(낮음) |
| `basis` | REQ-5, REQ-6 | `DB`(음식 DB) · `NAME_KEYWORD`(이름 속 재료 단어) · `COOKING_METHOD`(조리법에서 추정) · `RECIPE_SEARCH`(만개의 레시피) · `AI_ESTIMATE`(AI 추정) |
| `rule` | REQ-3, REQ-6 | 아래 [판정 규칙](#판정-규칙)의 `R0`~`R4` |

- 제한 항목은 위계가 있다. `pork`·`beef`는 `meat`에 속한다. `meat`를 켜면 `pork`·`beef` 재료도 걸린다. 재료는 가장 구체적인 항목 하나에 연결한다(돼지고기 → `pork`).
- 알레르기는 프리셋 없이 `egg`·`dairy`·`peanut`·`seafood` 토글로 고른다.

---

## REQ-4 사용자 번호 발급

`POST /api/v1/users` — 회원 정보 없이 사용자 번호를 하나 발급한다. 클라이언트는 `localStorage`에 `user_id`가 없을 때만 부른다.

### 요청

바디 없다.

### 응답 201 Created

```json
{
  "user_id": 1
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
    { "code": "HALAL", "name": "할랄", "items": ["pork", "alcohol"] },
    { "code": "VEGETARIAN", "name": "채식", "items": ["meat", "seafood"] }
  ],
  "items": [
    { "code": "meat", "name": "고기", "parent": null },
    { "code": "pork", "name": "돼지고기", "parent": "meat" },
    { "code": "beef", "name": "소고기", "parent": "meat" },
    { "code": "seafood", "name": "해산물", "parent": null },
    { "code": "egg", "name": "계란", "parent": null },
    { "code": "dairy", "name": "우유·유제품", "parent": null },
    { "code": "peanut", "name": "땅콩", "parent": null },
    { "code": "alcohol", "name": "술", "parent": null }
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
  "detail": "저장된 식단 제한이 없습니다."
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

- `preset`: 고르면 그 프리셋의 `items`가 켜진다. 프리셋을 풀려면 `null`
- `toggles`: 바뀐 항목만 보내도 된다(on-click 단위). 프리셋으로 켜진 항목도 토글로 끌 수 있다
- `version`: 마지막으로 조회한 버전. 처음 저장할 때는 생략한다

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
  "detail": "다른 요청이 먼저 처리되었습니다. 다시 시도해 주세요."
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| 같은 사용자의 저장이 동시에 들어옴(`version` 불일치, 처음 저장이 겹침) | 409 Conflict → 클라이언트는 다시 조회한 뒤 재요청 |
| 존재하지 않는 preset / 토글 코드 | 400 Bad Request |
| 발급되지 않은 `user_id` | 404 Not Found `{ "detail": "사용자 정보를 찾을 수 없습니다." }` → 클라이언트는 번호를 다시 발급받는다 |
| 서버 내부 오류 | 500 Internal Server Error |

---

## REQ-1 사진 업로드

`POST /api/v1/images` — 파일 선택, 드래그 앤 드롭, 모바일 카메라 촬영으로 고른 사진을 서버에 올리고 `image_id`를 받는다. 사진은 DB(`images.data`, bytea)에 저장한다.

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
| `file` 누락 | 400 Bad Request `{ "detail": "업로드할 사진이 없습니다." }` |
| 이미지가 아닌 파일 / 지원하지 않는 형식 | 415 Unsupported Media Type `{ "detail": "지원하지 않는 이미지 형식입니다." }` |
| 10MB 초과 | 413 Payload Too Large `{ "detail": "사진 용량이 너무 큽니다. (최대 10MB)" }` |
| 서버 내부 오류 | 500 Internal Server Error |

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
- 클라이언트는 `name`이 null이 아닌 항목만 모아 REQ-3에 보낸다
- **모든 `name`이 null이거나 `menus`가 비었으면** 판정 화면 대신 메뉴 이름 입력칸(OPT-1)을 보여 준다

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `image_id` 누락 | 400 Bad Request |
| 존재하지 않는 `image_id` | 404 Not Found `{ "detail": "이미지를 찾을 수 없습니다." }` |
| 메뉴판·음식 사진으로 인식되지 않음 | 422 Unprocessable Entity `{ "detail": "메뉴판이나 음식 사진이 아닙니다." }` → 클라이언트는 메뉴 이름 입력칸(OPT-1)을 보여 준다 |
| 일부 메뉴를 읽지 못함 | 200 OK, 해당 항목 `name: null` |
| 판독 엔진(AI) 오류·시간 초과 | 500 Internal Server Error → 클라이언트는 메뉴 이름 입력칸(OPT-1)을 보여 준다 |

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
      "reason": "돼지고기가 들어 있어 할랄 기준에 맞지 않습니다.",
      "matched_restrictions": ["pork"],
      "rule": "R1",
      "ingredients": [
        { "name": "돼지고기", "evidence": "CONFIRMED", "confidence": "high", "basis": "DB", "restriction": "pork" },
        { "name": "고추장", "evidence": "CONFIRMED", "confidence": "high", "basis": "DB", "restriction": null },
        { "name": "양파", "evidence": "CONFIRMED", "confidence": "high", "basis": "DB", "restriction": null }
      ]
    },
    {
      "menu_name": "미역국",
      "level": "CHECK",
      "reason": "맛술이 쓰였을 수 있어 할랄 기준을 확인해야 합니다.",
      "matched_restrictions": [],
      "rule": "R2",
      "ingredients": [
        { "name": "미역", "evidence": "CONFIRMED", "confidence": "high", "basis": "DB", "restriction": null },
        { "name": "소고기", "evidence": "CONFIRMED", "confidence": "high", "basis": "DB", "restriction": "beef" },
        { "name": "맛술", "evidence": "INFERRED", "confidence": "medium", "basis": "COOKING_METHOD", "restriction": "alcohol" }
      ]
    }
  ]
}
```

- `reason`: 근거 한 줄(배지 옆에 보인다)
- `matched_restrictions`: **확인된** 재료로 걸린 제한 항목(`BLOCKED`의 근거)
- `rule`: 적용된 판정 규칙(REQ-6)
- `ingredients[].restriction`: 그 재료가 연결된 제한 항목 코드(사용자와 상관없이 재료에 정해진 값). 어느 항목에도 걸리지 않는 재료는 `null`
- 예시는 할랄(`pork`·`alcohol`) 사용자 기준이다

### 판정 규칙

사용자가 켠 제한 항목(위계 포함)과 재료의 `restriction`을 맞대어 위에서부터 처음 맞는 규칙을 쓴다.

| 규칙 | 조건 | `level` |
| --- | --- | --- |
| `R0` | 사용자 제한이 하나도 없다(미설정·미등록 `user_id` 포함) | `SAFE` |
| `R1` | `CONFIRMED` 재료가 제한에 걸린다 | `BLOCKED` |
| `R2` | `INFERRED` 재료가 제한에 걸린다 | `CHECK` |
| `R3` | 걸리는 재료는 없지만 재료가 모두 `AI_ESTIMATE`에서 왔다 | `CHECK` |
| `R4` | 걸리는 재료가 없다 | `SAFE` |

- AI 추정(`AI_ESTIMATE`) 재료는 항상 `INFERRED`다. 그래서 AI 추정만으로는 `BLOCKED`가 나오지 않는다(REQ-5).
- 판정은 코드(`lib/judge/`)가 한다. AI는 재료를 찾을 때만 쓴다. 규칙마다 단위 테스트를 둔다.

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `menu_names`가 비었거나 빈 값·null을 포함 | 400 Bad Request `{ "detail": "입력 값이 없습니다." }` → 클라이언트는 같은 문구로 팝업 |
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
  "reason": "할랄 기준에 걸리는 재료를 찾지 못했습니다.",
  "matched_restrictions": [],
  "rule": "R4",
  "ingredients": [
    { "name": "된장", "evidence": "INFERRED", "confidence": "high", "basis": "RECIPE_SEARCH", "restriction": null },
    { "name": "멸치 육수", "evidence": "INFERRED", "confidence": "medium", "basis": "RECIPE_SEARCH", "restriction": "seafood" }
  ]
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `menu_name` 누락 또는 빈 문자열 | 400 Bad Request `{ "detail": "입력 값이 없습니다." }` → 클라이언트 팝업 |
| 음식 DB에 없는 메뉴 | REQ-5 순서로 재료를 찾아 판정, 200 OK (팝업 없음) |
| 서버 내부 오류 | 500 Internal Server Error |

---

## REQ-5 숨은 성분 추론

`POST /api/v1/ingredients/infer` — 음식 이름으로 재료를 찾는다. REQ-3·OPT-1이 내부에서 같은 함수를 부르고, 이 API는 그 함수를 직접 확인할 때 쓴다.

### 찾는 순서

1. **음식 DB**(`foods`): 등록된 재료는 `CONFIRMED`, `basis: DB`. 시드에 함께 넣은 숨은 재료(육수, 젓갈, 맛술 등)는 `INFERRED`, `basis: COOKING_METHOD`
2. **이름 속 재료 단어·조리법**: 이름의 재료 단어(예: "돈" → 돼지고기)는 `NAME_KEYWORD`, 조리법(예: "튀김" → 밀가루·계란)은 `COOKING_METHOD`. 모두 `INFERRED`
3. **만개의 레시피 검색**(1에 없을 때): 찾은 재료는 `INFERRED`, `basis: RECIPE_SEARCH`
4. **AI 추정**(3이 실패하거나 결과가 없을 때): `INFERRED`, `basis: AI_ESTIMATE`, `confidence`는 최대 `medium`

3·4에서 찾은 재료는 `foods`(`source`가 `recipe_search` 또는 `ai_estimate`)에 저장해 다음 요청부터 1에서 찾는다.

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
    { "name": "돼지고기", "evidence": "INFERRED", "confidence": "high", "basis": "NAME_KEYWORD", "restriction": "pork" },
    { "name": "밀가루", "evidence": "INFERRED", "confidence": "high", "basis": "COOKING_METHOD", "restriction": null },
    { "name": "계란", "evidence": "INFERRED", "confidence": "medium", "basis": "COOKING_METHOD", "restriction": "egg" }
  ]
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `menu_name` 누락 또는 빈 문자열 | 400 Bad Request `{ "detail": "입력 값이 없습니다." }` |
| 1~4 모두 재료를 찾지 못함 | 200 OK, `ingredients: []` → 판정은 `R3`과 같이 `CHECK` |
| 서버 내부 오류 | 500 Internal Server Error |

---

## OPT-2 음식점 추천

`GET /api/v1/restaurants/recommendations` — 판정 결과에 '안전' 메뉴가 하나도 없을 때 "다른 식당 보기"에서 부른다. 사용자가 켠 제한 항목을 모두 피하는 음식점을 돌려준다.

### 요청

| 위치 | key | 필수 | 설명 |
| --- | --- | --- | --- |
| Query | `user_id` | O | 사용자 번호 |
| Query | `limit` | X | 추천 개수 (기본 5) |

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
      "matched_restrictions": ["pork", "alcohol"]
    }
  ]
}
```

- `matched_restrictions`: 이 음식점이 피하는 항목 중 사용자 제한과 겹치는 것
- 음식점 데이터는 팀이 조사한 학교 주변 음식점 시드(`lib/db/seed/restaurants.ts`)다

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `user_id` 누락 | 400 Bad Request |
| `user_id`가 존재하지 않음 | 404 Not Found `{ "detail": "사용자 정보를 찾을 수 없습니다." }` |
| 조건에 맞는 음식점 없음 | 200 OK, `restaurants: []` → 화면에 '조건에 맞는 음식점이 없습니다.' |
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

1. **`user_id` 보호.** 로그인이 없어 `user_id`만 알면 남의 식단 제한을 조회·수정할 수 있다. 해커톤 범위에서는 그대로 간다([decisions.md](decisions.md)).
2. **AI 호출 남용.** 로그인이 없고 팀 AI 예산이 $10이라 호출 제한이 필요하다. 방식은 [tech-stack.md](tech-stack.md) 열린 결정 2.
3. **메뉴 이름 표기 차이.** 판독한 이름과 DB 이름의 띄어쓰기·동의어(`돈까스`/`돈가스`)는 `foods.aliases`로 맞춘다. 공백 제거 외의 정규화 규칙은 ③이 정한다.
