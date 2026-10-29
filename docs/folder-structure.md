# 폴더 구조

> **잠정.** 백엔드의 `domain/{controller, service, dto, entity, repository}`와 프런트엔드의 레이어드 방식은 팀이 정했다. 그 아래 세부는 **(제안)** 표시이고, 만들면서 바뀔 수 있다. 바뀌면 이 파일을 **그 변경과 같은 커밋에서** 고친다([ai-rules.md](ai-rules.md) 5번). 기술 스택은 [tech-stack.md](tech-stack.md)를 본다.

## 저장소 최상위 **(제안)**

```text
NULL-SUMTECH/
├── backend/            Spring Boot
├── frontend/           Next.js
├── e2e/                Playwright (키트 규칙: e2e/scenarios/<id>.spec.ts, e2e/fixtures/)
├── docs/               팀 협업 문서
├── docker-compose.yml
├── CLAUDE.md
├── README.md
└── progress.md         (저장소 루트에 둔다)
```

`e2e/`는 [progress.md](../progress.md) 3·5단계와 [scenarios.yaml](scenarios.yaml)이 루트의 `e2e/fixtures`를 가리키므로 루트에 둔다. `backend/`와 `frontend/` 이름은 제안이다.

## 백엔드

기준은 **도메인 우선**이다. 도메인 폴더 안에 계층(controller·service·dto·entity·repository)을 둔다. 도메인은 [api.md](api.md)의 도메인 열을 따른다.

```text
backend/src/main/java/<기본 패키지>/
├── domain/
│   ├── image/            사진 업로드 (REQ-1)
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dto/
│   │   ├── entity/
│   │   └── repository/
│   ├── menu/             메뉴 판독 (REQ-2)
│   ├── judgment/         3단계 판정, 수동 입력 판정 (REQ-3, OPT-1)
│   ├── restriction/      식단 제한 프리셋·저장 (REQ-4)
│   ├── ingredient/       숨은 성분 추론 (REQ-5, OPT-4)
│   └── restaurant/       음식점 추천 (OPT-2)
├── global/               (제안) 도메인에 속하지 않는 공통: config, exception, 공통 응답
└── <Application>.java
backend/src/main/resources/
├── application.yml
└── db/migration/         Flyway
    ├── V1__init.sql              표 변경(V 파일)은 역할 ①만 만든다
    ├── R__10_seed_restrictions.sql   시드는 반복 마이그레이션(R__). 번호 순서로 실행된다
    ├── R__20_seed_restaurants.sql
    └── R__30_seed_foods.sql
backend/src/test/java/<기본 패키지>/   main과 같은 패키지 구조 (JUnit 5)
```

- **기본 패키지는 잠정으로 `com.nullteam.foodcheck`다** (`TODO(확인)`). 팀 이름 `NULL`은 Java 예약어 `null`이라 패키지 이름으로 쓸 수 없다(예: `com.sumtech.null` 불가).
- 공통 코드는 `global/`에 둔다: `global/exception`(에러 응답 `{ "detail" }`), `global/health`(`GET /__health`).
- 시드(`R__` 파일)는 여러 번 실행돼도 결과가 같게 쓴다(`ON CONFLICT`). 내용을 고치면 Flyway가 다음 시작 때 다시 실행한다.
- 도메인 폴더 이름은 [api.md](api.md)의 도메인 열과 같고, 옆 주석의 기능 번호는 [problem.yaml](problem.yaml)의 `features`(REQ·OPT)다.

### 규칙

| 항목 | 규칙 |
| --- | --- |
| 의존 방향 | controller → service → repository. 반대로 부르지 않는다 |
| entity 노출 | entity는 controller 밖으로 나가지 않는다. 요청·응답은 dto로 주고받는다 |
| 도메인 간 호출 | 다른 도메인은 그 도메인의 service를 통해서만 부른다. 다른 도메인의 repository·entity를 직접 쓰지 않는다 **(제안)** |
| 비어 있는 계층 | DB를 안 쓰는 도메인(예: `menu`)은 `entity/`·`repository/`를 만들지 않는다. 필요해지면 만든다 **(제안)** |
| 이름 **(제안)** | `XxxController`, `XxxService`, `XxxRequest` / `XxxResponse`, `Xxx`(entity), `XxxRepository` |
| 응답 필드 | [api.md](api.md)의 JSON 필드 이름(snake_case)을 그대로 쓴다. 코드는 camelCase이므로 Jackson 설정으로 맞춘다 **(제안)** |

## 프런트엔드

**레이어드 방식**이다. 위 계층이 아래 계층만 부르고, 반대로는 부르지 않는다. 계층의 이름과 위치는 제안이다.

```text
frontend/src/
├── app/            화면 계층: App Router 라우트, 레이아웃, 페이지. 얇게 둔다
├── components/     화면 계층: 재사용 UI 컴포넌트
├── hooks/          애플리케이션 계층: 상태·흐름 (예: 업로드 → 판독 → 판정)
├── services/       데이터 계층: 백엔드 API 호출. api.md와 1:1
├── lib/            순수 유틸: 이미지 축소(OPT-3), 형식 변환
└── types/          API 요청·응답 타입
```

의존 방향: `app` → `components` → `hooks` → `services` → `lib` · `types`.

| 항목 | 규칙 |
| --- | --- |
| API 호출 | `fetch`는 `services/`에서만 한다. 컴포넌트·페이지가 직접 부르지 않는다. 주소는 같은 출처의 `/api/v1/...`이고, `app/api/v1/[...path]/route.ts`가 백엔드로 넘긴다 |
| 이미지 축소 | OPT-3은 백엔드 API가 아니라 `lib/`의 순수 함수로 두고, 업로드 직전에 `hooks/`에서 부른다 |
| 타입 | `types/`는 [api.md](api.md) 계약을 따른다. springdoc으로 타입을 생성하기로 하면 생성 결과를 둔다(`tech-stack.md` `미정`) |
| 고지 문구 | REQ-7은 모든 화면 하단에 보이므로 `app/layout`에 둔다 |

## 열린 질문

1. 백엔드 기본 패키지 이름. 잠정 `com.nullteam.foodcheck`.
2. `global/`을 쓸지, 공통 코드를 `common/`처럼 다른 이름으로 둘지. 뼈대는 `global/`로 만들었다.
3. 프런트엔드 레이어 이름과 개수(위 6개는 제안). 기능별 폴더(`features/`)를 섞을지.
4. ~~Dockerfile 위치~~ 잠정: `backend/Dockerfile`, `frontend/Dockerfile`, 루트 `docker-compose.yml`. `app`은 Next.js다([tech-stack.md](tech-stack.md) 열린 결정 1).
