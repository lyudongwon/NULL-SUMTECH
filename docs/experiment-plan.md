# 실험 계획

기획(`problem.yaml`의 `features`와 `measurable`)대로 만들었는지, 시나리오(`scenarios.yaml`)대로 써도 되는지를 판단하기 위한 실험을 적는다. 흐름 E2E 7개(`e2e/scenarios`)는 정상 경로만 보므로, 여기에는 그 밖의 완료 조건·성공 지표·실제 사용 환경을 둔다.

- 작성일: 2026-10-30
- 결과는 실험마다 아래 "기록할 곳"에 적고, 이 문서의 상태 칸을 고친다.
- 판정 규칙·문구·기대값은 `api.md`와 시드(`backend/src/main/resources/db/migration/R__*.sql`)가 원본이다. 원본이 바뀌면 이 문서의 기대값을 같이 고친다.

## 판단 기준

| 질문 | "예"라고 말하려면 |
| --- | --- |
| 기획대로 만들었는가 | A1·A2 지표 달성, B 규칙 표 전부 일치, C에서 완료 조건 위반 0건 |
| 시나리오대로 써도 되는가 | 흐름 E2E 7/7(완료, 2026-10-30), D1 실제 메뉴판 판독, D3 사용자 시험에서 막힘 없이 끝까지 감 |

위반이 나오면 원인을 셋 중 하나로 나눠 기록한다. ① 구현이 기획과 다름(코드를 고친다) ② 기획이 모호하거나 현실과 맞지 않음(`problem.yaml`·`api.md`를 고치고 `decisions.md`에 남긴다) ③ 테스트 자료 문제(fixture·시드를 고친다).

## 공통 준비

```powershell
docker compose down -v
$env:RELEASE_ID = git rev-parse HEAD; docker compose up -d --build
curl http://localhost:3000/__health   # releaseId가 지금 commit인지
```

- 빈 DB에서 시작한다. REQ-5가 찾아온 재료를 `foods`에 쌓기 때문에, 앞 실험이 남긴 데이터가 다음 실험 결과를 바꿀 수 있다.
- API 실험은 `POST /api/v1/users`로 실험마다 새 `user_id`를 받아 쓴다.
- AI 호출 수를 실험마다 적는다. 팀 예산은 누적 $10이다.

## A. 성공 지표 (problem.yaml `measurable`)

| ID | 실험 | 방법 | 통과 기준 | AI 호출 | 상태 |
| --- | --- | --- | --- | --- | --- |
| A1 | 판정 시간 | 휴대폰 화면(Pixel 7)에서 "Upload photo"로 `bibimbap.jpg`·`bibimguksu.jpg`를 각각 10회 올리고, 사진을 고른 때부터 판정 화면에 배지가 보일 때까지 잰다. Playwright로 시각을 찍어 측정자 손 오차를 없앤다 | 20회 모두 15초 이내 | 약 20 | 완료(시간 통과, 배지 흔들림) |
| A2 | 판정 정확도·안정성 | 배지가 정해진 시나리오 5개(`scan-menu-board`·`scan-food-photo-single-dish`·`diet-toggle-only`의 설렁탕 Avoid, `halal-bibimbap-gochujang`·`peanut-allergy-bibimguksu`의 Check first)를 5회씩 돌린다 | 25/25 배지 일치, Confirmed 제한 재료가 있는데 Safe인 경우 0건 | 약 25 | 완료(23/25, 실패) |

- A1은 반복마다 같은 메뉴를 다시 판독하므로 AI 판독 시간이 대부분이다. 최댓값·평균·중앙값을 같이 적는다.
- A2에서 배지가 한 번이라도 바뀌면 그 회차의 판독 결과(읽은 메뉴 이름)와 판정 근거를 남긴다. 판독이 흔들렸는지, 판정이 흔들렸는지를 나누기 위해서다.
- 기록할 곳: `docs/measurements.md`에 "3. 서비스 판정 시간", "4. 판정 정확도" 절을 추가한다.

## B. 판정 규칙 표 (REQ-3·REQ-4·REQ-5, AI 거의 안 씀)

`POST /api/v1/judgments`에 시드 메뉴 9개를 한 번에 넣고, 식단 프로필마다 배지와 규칙을 받아 아래 기대값과 비교한다. 기대값은 시드의 재료·확인/추정 구분과 판정 규칙 R0~R4로 계산했다.

### B1. 프리셋 × 시드 메뉴

| 메뉴 | 제한 없음 | Halal | Vegetarian | Vegan |
| --- | --- | --- | --- | --- |
| 설렁탕 | Safe R0 | Safe R4 | Avoid R1 | Avoid R1 |
| 콩나물국 | Safe R0 | Safe R4 | Check R2 | Check R2 |
| 그린샐러드 | Safe R0 | Safe R4 | Safe R4 | Safe R4 |
| 버섯리조또 | Safe R0 | Safe R4 | Check R2 | Avoid R1 |
| 김치찌개 | Safe R0 | Avoid R1 | Avoid R1 | Avoid R1 |
| 비빔밥 | Safe R0 | Check R2 | Check R2 | Avoid R1 |
| 비빔국수 | Safe R0 | Safe R4 | Safe R4 | Avoid R1 |
| 부대찌개 | Safe R0 | Avoid R1 | Avoid R1 | Avoid R1 |
| 떡볶이 | Safe R0 | Safe R4 | Avoid R1 | Avoid R1 |

계산 근거: Halal = 돼지고기·술, Vegetarian = 고기·해산물, Vegan = 고기·해산물·계란·유제품(`R__10`). 예를 들어 비빔국수는 계란이 Confirmed라 Vegan에서만 R1이고, 버섯리조또는 버터·치즈(유제품)가 Confirmed, 치킨 스톡(고기)이 Estimated라 Vegetarian에서 R2, Vegan에서 R1이다.

### B2. 토글 하나씩

| 토글 | 기대 결과(나머지 메뉴는 Safe R4) |
| --- | --- |
| Meat | 설렁탕·김치찌개·부대찌개 Avoid R1, 버섯리조또·비빔밥 Check R2 |
| Pork | 김치찌개·부대찌개 Avoid R1, 비빔밥 Check R2 |
| Beef | 설렁탕 Avoid R1, 부대찌개 Check R2 |
| Seafood | 떡볶이 Avoid R1, 콩나물국·김치찌개 Check R2 |
| Egg | 비빔밥·비빔국수 Avoid R1 |
| Dairy | 버섯리조또 Avoid R1 |
| Peanut | 비빔국수 Check R2 |
| Alcohol | 김치찌개 Check R2 |

### B3. 경계 경우

| ID | 입력 | 기대 |
| --- | --- | --- |
| B3-1 | Halal로 시드에 없는 메뉴 `된장찌개`를 수동 입력 | 팝업 없이 판정. 재료 출처가 `RECIPE_SEARCH`나 `AI_ESTIMATE`. 출처가 모두 `AI_ESTIMATE`면 Avoid가 나오지 않고 Check(R2 또는 R3) |
| B3-2 | B3-1 직후 같은 메뉴를 다시 입력 | `foods`에 저장된 재료로 바로 판정한다. 백엔드 로그에 만개의 레시피 검색과 AI 호출이 다시 찍히지 않는다 |
| B3-3 | 아무 재료도 나오지 않을 이름(예: `ㅁㄴㅇㄹ`)을 Halal로 입력 | 재료 목록이 비고 Check R3, 근거 "No ingredients were found. Please check before eating." |
| B3-4 | 제한 없는 사용자가 B3-3 입력 | Safe R0 |
| B3-5 | `menu_names`가 `[]`, `[""]`, `[null]` | 400, "Please enter a menu name." |
| B3-6 | 저장한 적 없는 새 사용자의 설정 화면 | 모든 토글이 꺼져 있다 |
| B3-7 | 같은 `version`으로 저장 요청 2개를 동시에 보냄 | 하나만 200, 다른 하나는 409 "Another request was saved first. Please try again." 화면에서도 같은 문구 |

- 기록할 곳: `docs/measurements.md` "5. 판정 규칙 표"에 실제 결과를 기대값 옆에 적는다. 다른 칸은 굵게 표시한다.
- AI 호출: B3-1·B3-3에서 몇 번만 쓴다.

## C. 완료 조건의 예외·화면 (REQ-1·2·7·8, OPT-2·3)

| ID | 기능 | 방법 | 기대 |
| --- | --- | --- | --- |
| C1 | REQ-1 형식 | 같은 음식 사진을 PNG·WEBP로 바꿔 화면에서 올림 | 판정 화면까지 감 |
| C2 | REQ-1 거부 | `.txt`, `.gif`, 10MB 넘는 파일, 파일 없이 `POST /images` | 각각 415 "This image format is not supported.", 413 "The photo is too large (max 10MB).", 400 "Please choose a photo to upload." 화면에도 같은 영어 문구 |
| C3 | REQ-1 드래그 앤 드롭 | 데스크톱 폭 화면에서 사진을 끌어다 놓음 | 판독이 시작됨 |
| C4 | REQ-2 추측 금지 | 흐름에 안 쓰는 `kimchi-jjigae.jpg`·`jeyuk-bokkeum.jpg`·`curry-rice.jpg`를 올림(각 2회) | 사진에 없는 메뉴를 지어내지 않음. 요리명은 1개. 시드에 없는 메뉴는 REQ-5로 판정 |
| C5 | REQ-2 시간 초과 | 판독 응답을 45초 넘게 늦추는 방법을 ②와 정한 뒤 확인(지금은 수단 없음) | "We couldn't read the menu. Please type the menu name."과 입력칸 |
| C6 | REQ-6 즉시 표시 | 판정 화면에서 메뉴를 누를 때 네트워크 요청을 봄 | 추가 요청 없이 근거 상세가 열림 |
| C7 | REQ-7·8 화면 전수 | 첫 화면·설정·사진 올리기·판정·입력칸·팝업·음식점 화면을 하나씩 열어 글자를 모음 | 모든 화면 하단에 고지 문구. 메뉴·재료 이름 말고는 한글이 없음 |
| C8 | OPT-2 판정 화면 노출 | Vegan으로 설렁탕 사진(Safe 0개) / Halal로 메뉴판 사진(Safe 있음) | 앞의 경우에만 판정 화면에 "Other restaurants" |
| C9 | OPT-2 빈 결과 | 모든 토글을 켜고 "Other restaurants" | "No restaurants match your diet yet." |
| C10 | OPT-3 축소 | 2000px 사진(`menu-board.jpg`)을 올리며 업로드 요청을 봄 | 보낸 이미지 긴 변이 1400px, JPEG. 886px 사진(`bibimguksu.jpg`)은 그대로 |

- C1·C2·C7은 화면과 API 둘 다 본다. API만 맞고 화면 문구가 빠지는 경우가 있어서다.
- **미리 보이는 문제:** 음식점 시드(`R__20`)가 아직 `(예시) 할랄 케밥` 같은 자리표시 5곳이고 주소가 `TODO(확인)`이다. OPT-2 완료 조건은 "팀이 직접 조사한 학교 주변 음식점"이므로 C8·C9가 통과해도 기획을 다 지킨 것은 아니다.
- 기록할 곳: 실패만 이 문서 아래 "결과"에 적고, 고칠 일은 담당 역할(`roles.md`)에 넘긴다.
- AI 호출: C1·C4·C8·C10에서 약 12회.

## D. 실제 사용 환경

| ID | 실험 | 누가 | 방법 | 통과 기준 | 언제 |
| --- | --- | --- | --- | --- | --- |
| D1 | 실제 학식 메뉴판 판독 | 팀(사진) + Agent(실행) | 학식당 메뉴판을 휴대폰으로 3~5장 찍어 `e2e/fixtures/real-*.jpg`로 넣음(반사·기울기 있는 그대로). 메뉴판에 적힌 메뉴를 사람이 먼저 적어 둠 | 사람이 적은 메뉴 대비 판독한 메뉴 비율(재현율)과 없는 메뉴를 지어낸 수. 목표는 팀이 사진을 본 뒤 정함 | 사진이 오면 바로 |
| D2 | 휴대폰 카메라 촬영 | 팀 | 배포 URL(HTTPS)을 실제 안드로이드·아이폰 브라우저로 열어 카메라로 찍고 판정까지 감 | 두 기기 모두 판정 화면까지 감, 큰 사진도 막히지 않음(OPT-3) | 첫 배포 뒤 |
| D3 | 사용자 시험 | 팀 + E1·E2(또는 대신할 사람) | 설명 없이 휴대폰을 주고 E1·E2 시나리오를 직접 해 보게 함. 시간·막힌 곳·한 말을 적음. 끝나고 "이 결과를 보고 먹겠는가, 직원에게 무엇을 물어보겠는가"를 물음 | 도움 없이 끝까지 감, 15초 목표 안, 근거를 보고 물어볼 재료(고추장·양념장)를 말할 수 있음 | 배포 뒤 |

- D1에 쓴 실제 사진은 저장소에 올려도 되는지(사람 얼굴·학교 이름 등) 먼저 확인한다. 안 되면 로컬에서만 돌리고 결과만 적는다.
- D3는 기획 의도("메뉴를 고르는 그 자리에서 먹을지 정한다")를 확인하는 유일한 실험이다. 결과는 `measurements.md`에 측정 방법·날짜·측정자와 함께 적는다.

## 순서와 비용

| 순서 | 실험 | 누가 | AI 호출(대략) |
| --- | --- | --- | --- |
| 1 | B 전부 | Agent | 5 |
| 2 | A1, A2 | Agent | 45 |
| 3 | C1~C4, C6~C10 | Agent | 12 |
| 4 | D1 | 팀 사진 → Agent | 5~10 |
| 5 | C5 | ②와 방법을 정한 뒤 | 1 |
| 6 | D2, D3 | 팀 | 10~20 |

- B를 먼저 하는 이유는 AI를 거의 쓰지 않고, 판정 엔진이 틀리면 A2 결과도 의미가 없어서다.
- 모두 합쳐 AI 호출 약 80~95회다. 실행 전에 대시보드에서 쓴 금액과 남은 금액을 확인하고, 실행 뒤 다시 확인해 차이를 `progress.md` 4단계에 적는다.

## 결과

2026-10-30, Agent가 로컬 docker(빈 DB)에서 실행. 숫자 표는 [measurements.md](measurements.md) 3~5절. 원인 분류: ① 구현 ② 기획 ③ 테스트 자료.

### 판단

- **기획대로 만들었는가: 대부분 예, 하나는 아니오.** 판정 엔진(B 132칸·경계 경우), 업로드·화면 완료 조건(C)은 기획과 맞다. 시간 지표(A1)도 달성했다. 하지만 정확도 지표(A2 23/25)를 못 채웠고, 원인이 E1 그 자체다.
- **시나리오대로 써도 되는가: 시드에 있는 이름으로 읽히는 메뉴만 예.** 실제 메뉴판·음식 사진은 잘 읽지만(D1), 메뉴 이름이 조금만 달라도 레시피 검색으로 넘어가 숨은 재료를 놓치고, 메뉴판에 적힌 재료도 쓰지 않는다.

### 실험별 결과

| ID | 결과 | 분류 | 후속 조치(담당) |
| --- | --- | --- | --- |
| A1 | 통과(20/20이 15초 이내, 평균 5.7초). 배지는 비빔밥 4/10, 비빔국수 9/10만 Check first | ① | 아래 F1 |
| A2 | **실패** 23/25(E1 3/5). Confirmed→Safe 0건 | ① | F1 |
| B1·B2 | 통과 132/132 | — | — |
| B3 | 7개 중 B3-3만 다름: 뜻 없는 입력(`ㅁㄴㅇㄹ`)에 만개의 레시피가 상관없는 레시피를 줘 Safe | ① | F3 |
| C1 PNG·WEBP | 통과 | — | — |
| C2 거부 | 통과(API 415·413·400, 화면 "This image format is not supported."). curl처럼 1MB 넘는 요청에 `Expect: 100-continue`를 붙이는 클라이언트는 프록시에서 502 | ① | F5 |
| C3 드래그 앤 드롭 | 통과 | — | — |
| C4 추측 금지 | 통과. 김치찌개·제육볶음·카레라이스를 각 2회 같은 이름으로 읽음, 지어낸 메뉴 없음 | — | — |
| C5 시간 초과 | 하지 않음(늦출 수단 없음). 판독 코드에 45초 제한은 있음 | — | ②와 방법 정하기 |
| C6 근거 상세 | 통과(누를 때 추가 요청 0) | — | — |
| C7 영어·고지 | 통과. 7개 화면 모두 고지 문구, 한글은 메뉴·재료 이름뿐. 단 음식점 화면의 시드 `(예시)`·`TODO(확인)` | ② | F4 |
| C8·C9 OPT-2 | 통과(Safe 없을 때만 판정 화면에 링크, 모든 토글이면 "No restaurants match your diet yet.") | — | F4 |
| C10 OPT-3 | 통과(2000×1500 → 서버가 받은 크기 1400×1050, 886px는 그대로) | — | — |
| D1 실제 사진 | 아래 표 | ①② | F1·F2·F3·F6 |
| D2·D3 | 하지 않음(배포·사람 필요) | — | 팀 |

### D1 실제 사진 (Wikimedia Commons, Halal + Peanut 사용자)

사진은 저장소에 넣지 않았다. 출처: [Menu of Starbucks in Seoul, 2001](https://commons.wikimedia.org/wiki/File:Menu_of_Starbucks_in_Seoul,_2001.jpg)(CC BY 2.0), [소설한남에서의 점심식사 03](https://commons.wikimedia.org/wiki/File:%EC%86%8C%EC%84%A4%ED%95%9C%EB%82%A8%EC%97%90%EC%84%9C%EC%9D%98_%EC%A0%90%EC%8B%AC%EC%8B%9D%EC%82%AC_03.jpg)(CC BY 2.0 KR), [오프레 런치 코스 03](https://commons.wikimedia.org/wiki/File:%EC%98%A4%ED%94%84%EB%A0%88_%EB%9F%B0%EC%B9%98_%EC%BD%94%EC%8A%A4_03.jpg)(CC BY 4.0), [South Korean Lunch Menu Item (1)](https://commons.wikimedia.org/wiki/File:South_Korean_Lunch_Menu_Item_(1).jpg)·[(2)](https://commons.wikimedia.org/wiki/File:South_Korean_Lunch_Menu_Item_(2).jpg)(CC BY-SA 4.0), [Rotary Korean Traditional Set Menu](https://commons.wikimedia.org/wiki/File:Rotary_Korean_Traditional_Set_Menu.jpg)(CC BY-SA 4.0), [Yanggakdo International Hotel Menu 02](https://commons.wikimedia.org/wiki/File:Yanggakdo_International_Hotel_Menu_02.JPG)(CC BY-SA 3.0).

| 사진 | 사람이 읽은 메뉴 | 서비스가 읽은 메뉴 | 시간 | 눈에 띈 것 |
| --- | --- | --- | --- | --- |
| 카페 벽 메뉴판(비스듬, 반사) | 17 | 16(빠짐 없음, "핫 초콜릿 (어린이용)"만 줄여 읽음) | 처음 60초 넘음 / 다시 22.5초 | 아이스 카페 아메리카노에 레시피 검색이 "베이컨"을 붙여 Check first |
| 한식 코스 메뉴 카드(작은 글씨) | 9 | 7(타락죽·잣무침 빠짐), 지어낸 것 없음 | 30.8초 | 카드에 "떡 – 막걸리", "후식 – 땅콩"이 적혀 있는데 둘 다 **Safe** |
| 프랑스·한국어 메뉴 카드 | 9 | 10(머리글 "런치 코스"를 메뉴로 읽음) | 36.9초 | "보리새우, 비트"처럼 재료 줄을 메뉴 이름으로 씀, "커피 또는 차"에 "미림" |
| 비빔밥(일반 그릇) 2장 | 비빔밥 | 비빔밥 | 3.9·5.5초 | Check first(기대대로) |
| 한상차림 | 여러 요리 | 김치찌개 1개 | 7.1초 | Avoid. 요리명 1개 규칙대로 가운데 찌개를 고름 |
| 국수 | 국수 | 칼국수 | 8.1초 | Safe |

### 후속 조치

| ID | 내용 | 담당 |
| --- | --- | --- |
| F1 | 판독 이름이 시드와 조금 달라도(`돌솥비빔밥`·`김치비빔국수`) 시드 메뉴로 이어지게 한다. 예: 판독 프롬프트에 기본 요리명을 따로 받기, 판정 전에 시드 이름이 들어 있는지 먼저 보기. 이미 `RECIPE_SEARCH`로 저장된 `돌솥비빔밥`·`김치비빔국수`는 지운다 | ②·③ |
| F2 | 메뉴판에 재료가 함께 적혀 있으면(코스 카드의 "막걸리", "땅콩") 판독에서 같이 받아 판정 근거로 쓸지 정한다. 지금은 버려져 Safe가 나온다 | 팀 결정 → ②·③ |
| F3 | 만개의 레시피 검색 결과가 메뉴 이름과 맞는지 확인한다(뜻 없는 입력에 상관없는 레시피, 아메리카노→베이컨, 커피→미림) | ③ |
| F4 | 음식점 시드를 팀이 조사한 실제 음식점으로 바꾼다(지금 `(예시)`·`TODO(확인)`) | ① |
| F5 | 프록시에서 `expect` 헤더도 빼고 넘긴다(`frontend/src/app/api/v1/[...path]/route.ts`의 HOP_BY_HOP) | ① |
| F6 | 처음 보는 메뉴가 많은 메뉴판은 재료 찾기가 메뉴마다 약 2초씩 차례로 돌아 60초를 넘길 수 있다. 동시에 찾거나 화면에 진행 상태를 보인다 | ③·① |
| F7 | 기획 질문: 시드에 없는 메뉴는 이름에 "돈"이 있어도 Estimated라 Halal에게 돈까스가 Check first다. 이대로 둘지 정한다 | 팀 결정 |

- AI 호출은 이번 실험에서 약 75회(판독 약 70, 재료 추정 몇 회)였다. 쓴 금액은 대시보드에서 확인해 progress.md 4단계에 적는다.
