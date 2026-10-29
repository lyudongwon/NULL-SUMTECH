# 결정 기록

채팅이나 회의에서 정한 결정을 한 줄로 남긴다. 최신 항목을 위에 둔다. 결정이 문서에 반영되면 "반영 상태"를 바꾼다. 규칙은 [ai-rules.md](ai-rules.md) 4번을 따른다.

| 날짜 | 결정 | 이유 | 반영 상태 |
| --- | --- | --- | --- |
| 2026-10-29 | (잠정, 역할 ①) 클라이언트는 `POST /api/v1/users`로 `user_id`를 발급받아 브라우저(`localStorage`의 `user_id`)에 보관한다. 서버에 없는 번호로 저장하면 404를 받고 새로 발급받는다 | `user_id`를 서버가 Auto Increment로 발급하기로 해서 발급 방법이 필요했다 | 반영: `api.md` REQ-4 사용자 번호 발급, `erd.md` 열린 질문 2 |
| 2026-10-29 | (잠정, 역할 ①) `docker compose`의 `app`은 Next.js(3000)다. 브라우저는 같은 출처 `/api/v1/**`을 부르고 Next.js가 `BACKEND_URL`로 백엔드에 넘긴다. API 기본 경로는 `/api/v1` | 화면과 백엔드를 나눠도 키트 조건(`app` 하나가 3000·`/__health`)을 지키고 CORS를 피한다 | 반영: `tech-stack.md` 열린 결정 1·2, `folder-structure.md`, `api.md` 공통, `docker-compose.yml` |
| 2026-10-29 | (잠정, 역할 ①) 백엔드 뼈대는 Java 21 + Spring Boot 4.1.2 + Gradle 9.8.2 + Spring AI 2.0.1, 기본 패키지 `com.nullteam.foodcheck`. 시드는 Flyway 반복 마이그레이션(`R__`)으로 역할별 파일에 쓴다 | 뼈대를 만들 때 정해야 했다. 팀이 다른 값을 고르면 바꾼다 | 반영: `tech-stack.md`, `folder-structure.md`, `roles.md` |
| 2026-10-29 | 음식 DB에 없는 메뉴는 만개의 레시피에서 재료를 검색해 판정한다. 이 경로는 흐름 `manual-input-unknown-menu`로 확인한다 | 팀 시나리오 5번의 "DB에 없으면 팝업"을 대신한다 | 반영: `problem.yaml > features > REQ-5`, `scenarios.yaml`. `roles.md`의 "E2E 메뉴는 시드에 넣는다"와 충돌해 확인 필요 |
| 2026-10-29 | 사용자 식별자는 `user_id`로 통일한다. 회원가입·로그인 없이 서버가 Auto Increment로 발급한다 | 회원을 두지 않기로 했다. 앞서 정한 `session_id`를 대신한다 | 반영: `problem.yaml > features > OPT-2`, `scenarios.yaml`, `erd.md`, `api.md`, `roles.md`. 처음 발급 방법은 `erd.md > 열린 질문` 2번 |
| 2026-10-29 | 백엔드는 `domain/<도메인>/{controller, service, dto, entity, repository}`, 프런트엔드는 레이어드 방식으로 폴더를 나눈다 | 팀이 폴더 구조의 기본을 정했다 | 반영: `folder-structure.md`. 그 아래 세부는 제안이고 기본 패키지 이름은 미정이다 |
| 2026-10-29 | 기술 스택은 작업 중 바뀔 수 있다. 바뀌면 같은 커밋에서 팀 협업 문서를 함께 고치고, 확정되면 `tech-stack.md`의 `미정`을 지운다 | 해커톤 중 실시간으로 바뀌는 선택이 많아, 문서와 코드가 따로 가면 어긋난다 | 반영: `ai-rules.md` 5번, `git-convention.md` 커밋 규칙 |
| 2026-10-29 | 백엔드는 Spring Boot, 화면은 Next.js로 나눈다. DB 접근은 JPA + Flyway, AI는 Spring AI, 검증은 Bean Validation, 판정 엔진 테스트는 JUnit 5를 쓴다 | 팀이 백엔드를 Spring Boot로 정했다. 노션 기술 스택 표의 Drizzle·Zod·Vercel AI SDK·Vitest는 TypeScript 서버 전제라 대체한다 | 반영: `tech-stack.md`, `CLAUDE.md`, `erd.md`. 노션 원본은 아직 안 바뀌었다. `app` 서비스를 어느 쪽으로 두는지 등 세부는 `tech-stack.md > 열린 결정` |
