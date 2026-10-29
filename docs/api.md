# API 명세

> 2026-10-29 노션 [기능 명세서 > 기능리스트](https://app.notion.com/p/408c4b3d7d9e80c59094ec4995ab33fb)의 10개 항목과 각 항목 상세 페이지(Request / Response / 예외 상황)를 옮긴 것이다. 노션이 원본이고 이 파일은 사본이다. 노션이 바뀌면 이 파일도 같은 커밋에서 고친다.
> 노션에 없는 값은 지어내지 않고 "미정"으로 두었다. 노션과 `docs/problem.yaml`이 어긋나는 부분은 임의로 맞추지 않고 맨 아래 [열린 질문](#열린-질문)에 모았다.

## 공통

| 항목 | 값 |
| --- | --- |
| 기본 경로 | `/api/v1` (잠정 결정, [decisions.md](decisions.md)). 아래 경로는 모두 이 접두사를 붙인다. 브라우저는 같은 출처로 부르고 Next.js가 백엔드로 넘긴다 |
| 인증 | 없다. 로그인 없이 쓰고(`docs/scenarios.yaml` `login: false`), 사용자는 클라이언트가 보관한 `user_id`(UUID)로 구분한다 |
| 요청 형식 | `application/json`. 사진 업로드(REQ-1)만 `multipart/form-data` |
| 에러 본문 | 모든 에러는 `{ "detail": "<사람이 읽는 한국어 문장>" }` |
| 서버 오류 | 모든 엔드포인트에서 서버 내부 오류는 500 Internal Server Error |

### 엔드포인트 목록

| 기능 ID | 기능 | 도메인 | 메서드 | 경로 | 구분 | 연관 화면 |
| --- | --- | --- | --- | --- | --- | --- |
| REQ-4 | 식단 제한 프리셋 목록 조회 | restriction | GET | `/restrictions/presets` | 주요 | 식단 제한 설정 |
| REQ-4 | 내 식단 제한 조회 | restriction | GET | `/users/{user_id}/restrictions` | 주요 | 식단 제한 설정 |
| REQ-4 | 식단 제한 저장 | restriction | PATCH | `/users/{user_id}/restrictions` | 주요 | 식단 제한 설정 |
| REQ-1 | 사진 업로드 | image | POST | `/images` | 주요 | 사진 업로드 |
| REQ-2 | 메뉴 판독 | menu | POST | `/menus/recognize` | 주요 | 판독 결과 |
| REQ-3 | 위험 정도 3단계 판정 | judgment | POST | `/judgments` | 주요 | 판정 결과 |
| OPT-1 | 수동 입력 판정 | judgment | POST | `/judgments/manual` | 부가 | 수동 입력 |
| OPT-4 | 숨은 성분 추론 | ingredient | POST | `/ingredients/infer` | 부가 | 판정 결과 |
| OPT-2 | 음식점 추천 | restaurant | GET | `/restaurants/recommendations` | 부가 | 음식점 추천 |
| OPT-3 | 이미지 축소 | client | — | 없음 (클라이언트 처리) | 부가 | 사진 업로드 |

### 열거형

| 필드 | 쓰이는 곳 | 값 |
| --- | --- | --- |
| `level` | REQ-3, OPT-1 | `SAFE`(안전) · `CHECK`(확인 필요) · `BLOCKED`(불가) |
| `image_type` | REQ-2 | `MENU_BOARD`(메뉴판, 메뉴 전부) · `FOOD`(음식 사진, `menus` 1개) |
| `basis` | OPT-4 | `NAME_KEYWORD`(이름 속 재료 단어) · `COOKING_METHOD`(조리법에서 추정) |
| `source` | REQ-1 | `FILE` · `DRAG_DROP` · `CAMERA` |
| `preset` | REQ-4 | `HALAL`(할랄) · `VEGETARIAN`(채식) · `VEGAN`(비건) (REQ-4 프리셋 목록 조회로 받아온다. `VEGETARIAN`은 `scenarios.yaml`의 "채식"을 위해 추가) |

---

## REQ-4 식단 제한 프리셋 목록 조회

`GET /restrictions/presets` — 설정 화면에 보여줄 프리셋(할랄, 비건 등)과 개별 토글 항목 목록을 조회한다.

### 요청

헤더·바디 없다.

### 응답 200 OK

```json
{
  "presets": [
    { "code": "HALAL", "name": "할랄", "items": ["pork", "alcohol"] },
    { "code": "VEGAN", "name": "비건", "items": ["meat", "seafood", "egg", "dairy"] }
  ],
  "items": [
    { "code": "pork", "name": "돼지고기" },
    { "code": "beef", "name": "소고기" },
    { "code": "seafood", "name": "해산물" },
    { "code": "peanut", "name": "땅콩" }
  ]
}
```

### 에러

노션 상세 페이지에 상태 코드 없이 `{ "detail": "..." }`만 적혀 있다. 500 외의 에러 응답은 미정이다.

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| 서버 내부 오류 | 500 Internal Server Error |

---

## REQ-4 내 식단 제한 조회

`GET /users/{user_id}/restrictions` — 사용자가 저장한 프리셋과 토글 상태를 불러온다.

### 요청

| 위치 | key | 설명 |
| --- | --- | --- |
| Path | `user_id` | 클라이언트가 저장한 UUID |

헤더 없다.

### 응답 200 OK

```json
{
  "user_id": "3f9b1c2e-7a41-4d2b-9c0e-5e8a1b2c3d4f",
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
| 아직 한 번도 저장하지 않은 `user_id` | 404 Not Found → 클라이언트는 기본값(제한 없음) 표시 |
| 서버 내부 오류 | 500 Internal Server Error |

---

## REQ-4 식단 제한 저장

`PATCH /users/{user_id}/restrictions` — 프리셋 선택과 토글 on-click으로 바뀐 제한 음식 설정을 저장한다. 처음 저장할 때 `user_id`를 등록한다.

### 요청

헤더

```json
{
  "Content-Type": "application/json"
}
```

바디

```json
{
  "preset": "HALAL",
  "toggles": { "peanut": true },
  "version": 3
}
```

- `toggles`: 바뀐 항목만 보내도 된다(on-click 단위)
- `version`: 마지막으로 조회한 버전. 처음 저장할 때는 생략한다

### 응답 200 OK

```json
{
  "user_id": "3f9b1c2e-7a41-4d2b-9c0e-5e8a1b2c3d4f",
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
| 같은 요청이 동시에 들어옴(`version` 불일치) | 409 Conflict → 클라이언트는 다시 조회한 뒤 재요청 |
| 존재하지 않는 preset / 토글 코드 | 400 Bad Request |
| 서버 내부 오류 | 500 Internal Server Error |

---

## REQ-1 사진 업로드

`POST /images` — 파일 선택, 드래그 앤 드롭, 모바일 카메라 촬영으로 고른 사진을 서버에 올리고 `image_id`를 받는다.

### 요청

헤더

```json
{
  "Content-Type": "multipart/form-data"
}
```

바디 (form-data)

| key | type | 필수 | 설명 |
| --- | --- | --- | --- |
| `file` | File | O | JPEG / PNG / WEBP 이미지 (클라이언트에서 OPT-3 축소 후 전송) |
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

### 에러

400 Bad Request

```json
{
  "detail": "업로드할 사진이 없습니다."
}
```

415 Unsupported Media Type

```json
{
  "detail": "지원하지 않는 이미지 형식입니다."
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `file` 누락 | 400 Bad Request |
| 이미지가 아닌 파일 / 지원하지 않는 형식 | 415 Unsupported Media Type |
| 파일 용량 초과 | 413 Payload Too Large (한도 값과 응답 본문은 미정) |
| 서버 내부 오류 | 500 Internal Server Error |

---

## REQ-2 메뉴 판독

`POST /menus/recognize` — 메뉴판 사진이면 메뉴 이름을 전부, 음식 사진이면 요리명 1개를 뽑는다.

### 요청

헤더

```json
{
  "Content-Type": "application/json"
}
```

바디

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

- `image_type`: `MENU_BOARD`(메뉴판, 메뉴 전부) · `FOOD`(음식 사진, `menus` 1개)
- 읽지 못한 항목은 추측하지 않고 `name: null`로 비워 둔다 → 클라이언트에서 OPT-1 수동 입력으로 유도한다

### 에러

404 Not Found

```json
{
  "detail": "이미지를 찾을 수 없습니다."
}
```

422 Unprocessable Entity

```json
{
  "detail": "메뉴판이나 음식 사진이 아닙니다."
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `image_id` 누락 | 400 Bad Request |
| 존재하지 않는 `image_id` | 404 Not Found |
| 메뉴판·음식 사진으로 인식되지 않음 | 422 Unprocessable Entity |
| 일부 메뉴를 읽지 못함 | 200 OK, 해당 항목 `name: null` |
| 판독 엔진 오류 | 500 Internal Server Error |

---

## REQ-3 위험 정도 3단계 판정

`POST /judgments` — 메뉴마다 사용자 식단 제한 기준으로 안전 / 확인 필요 / 불가 중 하나의 배지와 판정 근거 한 줄을 붙인다.

### 요청

헤더

```json
{
  "Content-Type": "application/json"
}
```

바디

```json
{
  "user_id": "3f9b1c2e-7a41-4d2b-9c0e-5e8a1b2c3d4f",
  "menu_names": ["제육볶음", "미역국"]
}
```

- `menu_names`의 각 값은 null이 아니어야 한다. REQ-2에서 `name: null`인 항목은 빼고 보낸다

### 응답 200 OK

```json
{
  "results": [
    {
      "menu_name": "제육볶음",
      "level": "BLOCKED",
      "reason": "돼지고기가 들어 있어 할랄 기준에 맞지 않습니다.",
      "matched_restrictions": ["pork"],
      "ingredients": ["돼지고기", "고추장", "양파"]
    },
    {
      "menu_name": "미역국",
      "level": "CHECK",
      "reason": "육수 재료가 확인되지 않았습니다.",
      "matched_restrictions": [],
      "ingredients": ["미역", "참기름"]
    }
  ]
}
```

- `level`: `SAFE`(안전) · `CHECK`(확인 필요) · `BLOCKED`(불가)
- `reason`: 부가 기능. 판정에 따른 근거 한 줄

### 에러

400 Bad Request

```json
{
  "detail": "음식 이름이 비어 있습니다."
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `menu_names`가 비었거나 null 값을 포함 | 400 Bad Request → 클라이언트 popup 이벤트 발생 |
| 미등록 `user_id`(식단 제한 미설정) | 제한 없음 기준으로 판정, 200 OK |
| DB에 없는 메뉴 | 200 OK, `level: CHECK`, OPT-4 추론 결과 반영 |
| 서버 내부 오류 | 500 Internal Server Error |

---

## OPT-1 수동 입력 판정

`POST /judgments/manual` — 판독에 실패하면 메뉴 이름을 직접 입력받아 REQ-3과 같은 엔진으로 판정한다.

### 요청

헤더

```json
{
  "Content-Type": "application/json"
}
```

바디

```json
{
  "user_id": "3f9b1c2e-7a41-4d2b-9c0e-5e8a1b2c3d4f",
  "menu_name": "돈가스"
}
```

### 응답 200 OK

```json
{
  "menu_name": "돈가스",
  "level": "BLOCKED",
  "reason": "돼지고기가 들어 있어 할랄 기준에 맞지 않습니다.",
  "matched_restrictions": ["pork"],
  "ingredients": ["돼지고기", "빵가루", "밀가루", "계란"]
}
```

- 응답 형식은 REQ-3 `results`의 한 항목과 같다

### 에러

404 Not Found

```json
{
  "detail": "등록되지 않은 메뉴입니다."
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `menu_name` 누락 또는 빈 문자열 | 400 Bad Request |
| 데이터베이스에 음식 이름이 없음 | 404 Not Found → 클라이언트 popup으로 알림 |
| 서버 내부 오류 | 500 Internal Server Error |

---

## OPT-4 숨은 성분 추론

`POST /ingredients/infer` — 음식 이름에 들어 있는 재료 단어와 조리법으로 겉으로 드러나지 않는 재료를 추정한다.

### 요청

헤더

```json
{
  "Content-Type": "application/json"
}
```

바디

```json
{
  "menu_name": "돈가스"
}
```

### 응답 200 OK

```json
{
  "menu_name": "돈가스",
  "inferred_ingredients": [
    { "name": "돼지고기", "basis": "NAME_KEYWORD" },
    { "name": "밀가루", "basis": "COOKING_METHOD" },
    { "name": "계란", "basis": "COOKING_METHOD" }
  ]
}
```

- `basis`: `NAME_KEYWORD`(이름 속 재료 단어) · `COOKING_METHOD`(조리법에서 추정)
- REQ-3 판정 때 내부적으로도 호출되어 `ingredients`에 반영된다

### 에러

404 Not Found

```json
{
  "detail": "등록되지 않은 메뉴라 추론을 실행하지 않았습니다."
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `menu_name` 누락 | 400 Bad Request |
| 데이터베이스에 음식 이름이 없음 | 404 Not Found, 추론 미실행 |
| 서버 내부 오류 | 500 Internal Server Error |

---

## OPT-2 음식점 추천

`GET /restaurants/recommendations` — 학식을 이용할 수 없는 경우 사용자 식단 제한에 맞는 다른 음식점을 추천한다.

### 요청

| 위치 | key | 필수 | 설명 |
| --- | --- | --- | --- |
| Query | `user_id` | O | 클라이언트가 저장한 UUID |
| Query | `limit` | X | 추천 개수 (기본 5) |

헤더 없다.

```text
GET /api/v1/restaurants/recommendations?user_id=3f9b...&limit=5
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

### 에러

404 Not Found

```json
{
  "detail": "사용자 정보를 찾을 수 없습니다."
}
```

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| `user_id` 누락 | 400 Bad Request |
| `user_id`가 존재하지 않음 | 404 Not Found |
| 조건에 맞는 음식점 없음 | 200 OK, `restaurants: []` |
| 서버 내부 오류 | 500 Internal Server Error |

---

## OPT-3 이미지 축소 (클라이언트 처리)

별도 API가 없다. 프론트엔드에서 REQ-1 `POST /images`를 호출하기 직전에 처리한다.

| 항목 | 값 |
| --- | --- |
| 입력 | 원본 이미지 |
| 리사이즈 기준 | 긴 변 1400px (비율 유지) |
| 출력 형식 | JPEG, 품질 0.85 |
| 출력 | REQ-1 `file` 필드로 전송 |

### 예외 상황

| 예외 상황 | 처리 방식 |
| --- | --- |
| 긴 변이 1400px 미만 | 축소하지 않고 원본 그대로 전송 |

---

## 열린 질문

노션 기능리스트와 `docs/problem.yaml`을 옮기면서 어긋나거나 비어 있던 것들이다. 임의로 맞추지 않았으니 팀에 확인하고 고친다.

1. ~~**기본 경로.**~~ 잠정으로 모든 경로에 `/api/v1`을 붙이기로 했다([decisions.md](decisions.md)).
2. **경로 체계가 `problem.yaml`과 다르다.** `problem.yaml`은 F1에 `POST /api/scan`, F6에 `POST /api/judge-names`로 적혀 있고, 노션은 `POST /images` + `POST /menus/recognize` + `POST /judgments`로 나뉘어 있다. 어느 쪽이 최신인가?
3. **API가 없는 기능.** `problem.yaml`의 F6(프로필 전환), F7(판정 근거 상세), F10(고지 문구)에 해당하는 항목이 노션 기능리스트에 없다. F6은 REQ-3 재호출로, F7은 REQ-3 응답의 `ingredients`·`matched_restrictions`로 덮이는지 확인이 필요하다.
4. **확신도 필드가 없다.** `problem.yaml`의 출력 스키마에는 `confidence(high|medium|low)`와 `hidden_ingredients`가 있는데, REQ-3·OPT-1 응답 예시에는 `level`·`reason`·`matched_restrictions`·`ingredients`만 있다. 성분별 확인/추정 구분(F4)도 응답에 없다.
5. **OPT-4와 REQ-3의 예외 처리가 어긋난다.** REQ-3은 "DB에 없는 메뉴 → 200 OK, `level: CHECK`, OPT-4 추론 결과 반영"이라고 하는데, OPT-4는 등록되지 않은 메뉴에 404를 주고 추론을 실행하지 않는다. 반영할 결과가 없다.
6. **프리셋 목록 조회의 에러.** 상태 코드 없이 `{ "detail": "..." }`만 적혀 있다.
7. **프리셋·토글 코드 목록이 두 문서에서 다르다.** 노션은 프리셋 `HALAL`·`VEGAN` 2종에 항목 코드 `pork`·`beef`·`seafood`·`peanut`·`alcohol`·`meat`·`egg`·`dairy`가 섞여 나온다. `problem.yaml` F8은 프리셋 3종(알레르기·할랄·채식)과 9개 그룹(계란, 우유, 견과, 돼지고기, 술, 젤라틴, 고기, 생선·해산물, 육수)을 든다. 알레르기 프리셋과 젤라틴·육수 코드가 노션에 없다.
8. **업로드 용량 한도.** `problem.yaml` F2는 12MB를 넘는 요청을 받지 않는다고 적혀 있고, 노션에는 413을 준다는 말만 있고 한도 값이 없다.
9. **`user_id` 보호.** 로그인이 없어 `user_id`만 알면 남의 식단 제한을 조회·수정할 수 있다. 그대로 가는가?
