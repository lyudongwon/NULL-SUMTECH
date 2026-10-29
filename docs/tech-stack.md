# 기술 스택

> **잠정.** 2026-10-29 팀 논의로 백엔드를 Spring Boot로 정했다(노션 기능 명세서 표는 Next.js 단일 프로젝트 전제였다). 작업하면서 바뀔 수 있고, 바뀌면 이 파일과 연관 문서를 **그 변경과 같은 커밋에서** 고친다. 확정되면 아래 "상태" 열의 `미정`을 지우고 노션 원본에도 반영한다. 노션과 어긋나면 레포 기준으로 진행하고 어긋난 위치를 보고한다([ai-rules.md](ai-rules.md) 4번).

상태: `사용` = 쓰기로 했다. `미정` = 쓰기로 했지만 세부(버전, 연결 방식 등)가 아직 안 정해졌다.

## 구성

화면(Next.js)과 API(Spring Boot)를 따로 만든다. 두 쪽의 계약은 [api.md](api.md)다.

| 영역 | 선택 | 상태 | 메모 |
| --- | --- | --- | --- |
| 백엔드 언어·프레임워크 | Java 21 + Spring Boot 4.1.2, Gradle 9.8.2(wrapper, Groovy DSL) | 사용 | 뼈대(`backend/`)에 쓴 잠정값. 기본 패키지 `com.nullteam.foodcheck`는 `TODO(확인)` |
| 화면 | TypeScript + Next.js 15.5 (App Router), React 19.1, Tailwind CSS 4 | 사용 | 모바일 웹. `frontend/` |
| DB | PostgreSQL | 사용 | 호스트(Neon 또는 AWS RDS)는 `미정` |
| ORM | JPA(Hibernate) | 사용 | |
| 마이그레이션 | Flyway | 사용 | 시작 시 자동 실행되어 시작 명령을 서버 실행만으로 유지한다. `ddl-auto`는 `validate` |
| AI | Spring AI 2.0.1(구조화 출력) + AI Gateway | 미정 | `AI_GATEWAY_API_KEY`를 읽는다. Gateway가 OpenAI 호환 엔드포인트를 주면 Spring AI의 OpenAI 모듈에서 base-url만 바꿔 쓴다. 되는지 확인 전이다. 안 되면 모델사 SDK나 키를 직접 쓴다 |
| 검증 | Jakarta Bean Validation + Jackson | 사용 | AI 응답과 API 입력의 형식 검사 |
| API 명세 연동 | springdoc-openapi → TypeScript 타입 생성 | 미정 | 두 언어 사이 어긋남을 줄이려는 제안 |
| 테스트 | JUnit 5(판정 엔진) + Playwright(E2E) | 사용 | Playwright는 화면 쪽 |
| 사진 저장 | S3, Postgres `bytea`, 또는 업로드·판독을 한 요청으로 합침 | 미정 | [erd.md](erd.md) 열린 질문 1 |
| 배포 | 화면은 Vercel, 백엔드는 AWS 컨테이너 | 미정 | 아래 "열린 결정" 1 |

## 실행 조건

[progress.md](../progress.md)의 3단계(최소 화면 docker 실행·CI·첫 배포)에서 옮긴 것이다. 이 조건은 Next.js 하나를 전제로 쓰였다.

- `docker compose`의 `app`은 컨테이너 안 3000 포트에서 받는다. `app`만 `ports`를 갖고 `env_file`은 쓰지 않는다.
- `GET /__health`는 인증·리다이렉트 없이 200을 준다. Spring Boot에서는 컨트롤러로 직접 만든다. Next.js를 쓸 때는 `app/%5F%5Fhealth`다.
- 빌드는 Dockerfile에서 끝내고 시작 명령은 서버 실행만 한다. Flyway가 시작 시 마이그레이션을 실행하는 이유가 이것이다.
- `docker compose up --build`만으로 빈 DB에서 앱과 시드 데이터가 2분 안에 떠야 한다. JVM 기동 시간을 포함해 확인한다. 컨테이너 메모리는 1GB 이상을 권한다.
- 환경변수 이름은 [README.md](../README.md)의 표를 따른다(`AI_GATEWAY_API_KEY` 등).

## 열린 결정

1. **어느 서비스가 `app`인가.** **잠정: Next.js가 `app`이다.** 3000 포트에서 받고 `/__health`(`frontend/src/app/%5F%5Fhealth`)를 준다. 백엔드(`backend`, 8080)와 DB는 `ports` 없이 내부 네트워크로만 쓴다. 팀이 다른 안을 고르면 `docker-compose.yml`과 이 항목을 같이 고친다.
   - 남은 안: Spring Boot가 `app`이 되어 3000 포트에서 받고 화면을 정적 빌드로 함께 서빙한다.

   배포(Vercel + AWS)도 이 구성에 맞춰 정한다.
2. **브라우저가 백엔드를 직접 부르는가.** **잠정: 직접 부르지 않는다.** 브라우저는 같은 출처의 `/api/v1/**`을 부르고, Next.js 라우트(`frontend/src/app/api/v1/[...path]/route.ts`)가 실행 시점의 `BACKEND_URL`로 넘긴다. CORS가 필요 없다. Vercel에 배포하면 이 프록시를 거치므로 요청 본문 4.5MB 제한이 걸린다. OPT-3으로 줄인 사진은 이보다 작다.
3. **업로드 한도.** Spring 기본값은 `spring.servlet.multipart.max-file-size` 1MB라 사진이 막힌다. 한도 값은 정해지지 않았다([api.md](api.md) 열린 질문 8).
4. **AI 호출 남용 방지.** 로그인이 없고 팀 AI 예산이 $10이라 세션 단위 호출 제한이 필요하다(예: Bucket4j). 방식은 `미정`.
5. **REQ-5의 만개의 레시피 검색 수단.** HTML 파싱이라면 Jsoup이 후보다. 사이트 차단과 이용약관 문제가 있다.
6. **E2E에서 AI 응답을 어떻게 다루는가.** 실제 비전 모델은 결과가 흔들리고 비용이 든다. 판독 응답을 고정하는 방식을 정하고, [ai-rules.md](ai-rules.md) 8번(실패를 mock으로 우회하지 않는다)과 구분해서 적는다.

DB 구조는 [erd.md](erd.md)를 본다.
