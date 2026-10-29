# 기술 스택

> 2026-10-30 `main` 기준 실제 구성이다. 2026-10-29 팀 논의에서 백엔드를 Spring Boot로 정했다(노션 기능 명세서 표는 Next.js 단일 프로젝트를 전제로 했다). 작업하다 바뀌면 이 파일과 관련 문서를 그 변경과 같은 커밋에서 고친다. 노션에도 반영한다. 노션과 어긋나면 레포 기준으로 진행하고 어긋난 곳을 알린다([ai-rules.md](ai-rules.md) 4번).

상태 열의 `사용`은 지금 코드에서 쓰는 것, `안 씀`은 검토했지만 도입하지 않은 것이다.

## 구성

화면(Next.js)과 API(Spring Boot)는 따로 만들고, 둘 사이의 약속은 [api.md](api.md)에 있다.

| 영역 | 선택 | 상태 | 메모 |
| --- | --- | --- | --- |
| 백엔드 언어·프레임워크 | Java 21 + Spring Boot 4.1.2, Gradle 9.8.2(wrapper, Groovy DSL) | 사용 | 기본 패키지 `com.nullteam.foodcheck` |
| 화면 | TypeScript + Next.js 15.5 (App Router), React 19.1, Tailwind CSS 4 | 사용 | 모바일 웹. `frontend/` |
| DB | PostgreSQL | 사용 | 배포 DB는 Render Postgres(무료, 만든 뒤 30일 만료). 백엔드는 Render가 주는 `DATABASE_URL`을 시작할 때 JDBC 설정으로 바꾼다 |
| ORM | JPA(Hibernate) | 사용 | |
| 마이그레이션 | Flyway | 사용 | 앱이 시작할 때 자동으로 돌아서 시작 명령은 서버 실행 하나로 끝남. `ddl-auto`는 `validate` |
| AI | Spring AI 2.0.1(구조화 출력) + AI Gateway | 사용 | 메뉴 판독(REQ-2, 비전)과 재료 AI 추정(REQ-5)에만 씀. 판정 규칙은 AI가 아니라 코드가 맡음. 키는 `AI_GATEWAY_API_KEY`. Spring AI의 OpenAI 모듈(`spring-ai-starter-model-openai`)에서 base-url만 Gateway(`https://ai-gateway.vercel.sh/v1`)로 바꿔 쓴다. 판독 모델 기본값은 `openai/gpt-5-mini`(`MENU_AI_MODEL`), 재료 추정도 같은 기본값(`INGREDIENT_AI_MODEL`, 20초 제한). 다시 시도는 한 번만(`spring.ai.retry.max-attempts: 2`). 사진 판독(비전)은 흐름 E2E(S1·S2)에서 동작한다 |
| 검증 | Jakarta Bean Validation + Jackson | 사용 | AI 응답과 API 입력의 형식 검사 |
| API 명세 연동 | springdoc-openapi → TypeScript 타입 생성 | 안 씀 | 두 언어 사이 어긋남을 줄이자는 제안이었고 도입하지 않았다. 화면 타입은 `frontend/src/types`에 [api.md](api.md)를 보고 맞춘다 |
| 테스트 | JUnit 5(판정 엔진) + Playwright(E2E) | 사용 | Playwright는 화면 쪽 |
| 사진 저장 | 백엔드 메모리(최대 200장, 30분) | 사용 | `ImageBodyStore`. 서버를 다시 켜면 사라진다. DB(`bytea`)로 옮길지는 [erd.md](erd.md) 열린 질문 1 |
| 배포 | Render(화면·백엔드 Docker 웹 서비스 + Postgres) | 사용 | 루트 `render.yaml` Blueprint. `docker-compose.yml`과 같은 Dockerfile을 쓰고, `/__health`의 `releaseId`는 Render의 `RENDER_GIT_COMMIT`이다. 채점은 docker compose로 띄운 앱을 본다 |

## 실행 조건

[progress.md](../progress.md) 3단계(최소 화면 docker 실행·CI·첫 배포)에서 옮겨 왔다. 원래 Next.js 하나를 전제로 쓴 조건이다.

- `docker compose`의 `app`은 컨테이너 안 3000 포트에서 받는다. `ports`는 `app`에만 두고 `env_file`은 쓰지 않는다.
- `GET /__health`는 인증·리다이렉트 없이 200을 돌려준다. Spring Boot라면 컨트롤러로 직접 만들고, Next.js라면 `app/%5F%5Fhealth`에 둔다.
- 빌드는 Dockerfile에서 끝내고 시작 명령은 서버 실행만 한다. Flyway를 시작할 때 돌리는 것도 이 때문이다.
- 빈 DB에서 `docker compose up --build` 한 번으로 앱과 시드 데이터가 2분 안에 떠야 한다. JVM 기동 시간까지 넣어서 확인하고, 컨테이너 메모리는 1GB 이상을 권한다.
- 환경변수 이름은 [README.md](../README.md)의 표를 따른다(`AI_GATEWAY_API_KEY` 등).

## 열린 결정

1. ~~어느 서비스가 `app`인가~~ — Next.js로 정했다. 3000 포트에서 받고 `/__health`(`frontend/src/app/%5F%5Fhealth`)를 준다. 백엔드(`backend`, 8080)와 DB는 `ports` 없이 내부 네트워크로만 쓴다. 팀이 다른 안을 고르면 `docker-compose.yml`과 이 항목을 같이 고친다.

   배포는 Render로 정했다(2026-10-30). 화면(`null-sumtech`)과 백엔드(`null-sumtech-api`)를 각각 Docker 웹 서비스로 띄운다. 무료 웹 서비스는 내부 네트워크로 요청을 받지 못해 화면의 `BACKEND_URL`은 백엔드의 공개 주소다.
2. ~~브라우저가 백엔드를 직접 부르는가~~ — 부르지 않는다. 브라우저는 같은 출처의 `/api/v1/**`을 부르고, Next.js 라우트(`frontend/src/app/api/v1/[...path]/route.ts`)가 실행 시점의 `BACKEND_URL`로 넘겨 준다. 그래서 CORS가 필요 없다.
3. ~~업로드 한도~~ — 10MB로 정했다([api.md](api.md) REQ-1). Spring 기본값 `spring.servlet.multipart.max-file-size` 1MB를 `application.yml`에서 10MB로 올렸다(`max-file-size`·`max-request-size`).
4. AI 호출 남용 방지 — 로그인 없이도 쓸 수 있고(로그인은 선택, OPT-4) 팀 AI 예산이 $10이라 호출 제한이 필요하다(예: Bucket4j). 아직 호출 제한은 없고 방식도 정하지 않았다.
5. ~~REQ-5의 만개의 레시피 검색 수단~~ — JDK HttpClient로 레시피 페이지의 JSON-LD(`recipeIngredient`)를 읽는다(Jsoup은 쓰지 않음, `RecipeSearchClient`). robots.txt는 2026-10-29에 확인했고 이용약관 검토는 남아 있다. 검색이 실패해도 AI 추정으로 넘어가니 기능이 멈추지는 않는다([api.md](api.md) REQ-5).
6. E2E에서 AI 응답을 어떻게 다루는가 — 실제 모델을 부르고 결과를 기다려 확인한다([progress.md](../progress.md) 6단계). 판정이 흔들릴 수 있는 메뉴는 기대 결과를 "세 배지 중 하나"로 쓰고, 판정을 고정해야 하는 메뉴(설렁탕)는 시드 재료로 R1이 나오게 한다. 판독 응답을 고정할지는 ②가 정한다.

DB 구조는 [erd.md](erd.md)에 있다.
