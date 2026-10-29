# 결정 기록

채팅이나 회의에서 정한 결정을 한 줄로 남긴다. 최신 항목을 위에 둔다. 결정이 문서에 반영되면 "반영 상태"를 바꾼다. 규칙은 [ai-rules.md](ai-rules.md) 4번을 따른다.

| 날짜 | 결정 | 이유 | 반영 상태 |
| --- | --- | --- | --- |
| 2026-10-29 | 모든 변경은 별도 브랜치에서 작업해 `main`을 향한 PR로 합친다. `main`에서 직접 작업해 push하는 것만 사용자 동의가 필요하다. 커밋·PR 범위는 작게 두고, 작업 단위가 끝날 때마다 최신 `main`을 확인해 크게 달라진 지점을 반영한다 | 팀원이 같은 뼈대를 `main`에 먼저 올려 중복·충돌이 생겼다 | 반영: `ai-rules.md` 11번, `git-convention.md` 브랜치·PR·작업 흐름·하지 않는 것 |
| 2026-10-29 | **문서 통일.** 원본은 레포(`problem.yaml` > `scenarios.yaml` > `api.md` 순)이고, 노션 통합 페이지는 레포를 옮겨 적는 읽기용이다. 기술 스택은 `main`에 올라온 Spring Boot 백엔드 + Next.js 화면을 그대로 쓴다 | 노션·레포 문서가 서로 어긋났다. 세 세션의 Claude가 읽는 곳이 레포다 | 반영: 전 문서, `api.md` 머리말 |
| 2026-10-29 | 음식 DB에 없는 메뉴는 이름·조리법 → 만개의 레시피 → AI 추정 순으로 재료를 찾는다. AI 추정만으로는 '확인 필요'를 넘지 않는다. '등록되지 않은 메뉴' 404·팝업은 없앤다. 찾은 재료는 `foods`에 저장한다 | 외부 사이트가 막혀도 판정이 멈추지 않게 한다. 시나리오의 "팝업 없이 판정"과 맞춘다 | 반영: `problem.yaml > features > REQ-5`, `api.md > REQ-3·OPT-1·REQ-5`, `erd.md` |
| 2026-10-29 | E2E 메뉴는 시드에 넣는다. 예외는 `manual-input-unknown-menu`의 된장찌개 하나다 | DB에 없는 경로를 확인해야 하고, AI 추정 덕분에 외부 사이트 없이도 배지가 나온다 | 반영: `roles.md`, `erd.md`, `scenarios.yaml`. 아래 "충돌해 확인 필요"를 해소한다 |
| 2026-10-29 | 프리셋은 `HALAL`(돼지고기·술), `VEGETARIAN`(채식: 고기·해산물, 계란·유제품 허용), `VEGAN`(비건)이다. 알레르기는 토글(`egg`·`dairy`·`peanut`·`seafood`)로 고른다. 재료는 걸리는 항목 전부에 연결한다(돼지고기 → `pork`·`meat`) | 시나리오의 "채식"·"할랄"·"고기"와 코드를 맞춘다. `R__10` 시드와 같다 | 반영: `api.md > 열거형`, `erd.md`, `problem.yaml > features > REQ-4` |
| 2026-10-29 | 판정 규칙은 R0(제한 없음 → 안전), R1(확인된 재료가 걸림 → 불가), R2(추정 재료가 걸림 → 확인 필요), R3(재료가 모두 AI 추정 → 확인 필요), R4(걸림 없음 → 안전)이다. 판정 응답에 REQ-6 근거 상세(`rule`, 재료별 `evidence`·`confidence`·`basis`·`restrictions`)를 함께 담는다 | REQ-6 필드가 API에 없었다. 추가 요청 없이 상세를 보여 준다 | 반영: `api.md > REQ-3` |
| 2026-10-29 | 업로드 한도는 10MB다. `user_id`만 알면 남의 설정을 볼 수 있는 점은 해커톤 범위에서 받아들인다 | 한도가 비어 있었고 Spring 기본값 1MB로는 사진이 막힌다 | 반영: `api.md > REQ-1`, `tech-stack.md > 열린 결정` 3. `application.yml` 변경은 ① |
| 2026-10-29 | 빈 메뉴 처리: 판독 결과가 전부 비었거나 422·500이면 메뉴 이름 입력칸(OPT-1)을 보여 주고, 판정 요청의 이름이 비었으면 '입력 값이 없습니다.' 팝업을 띄운다 | `problem.yaml`, `api.md`, 시나리오가 서로 달랐다 | 반영: `problem.yaml > features > REQ-2·REQ-3`, `api.md > REQ-2·REQ-3` |
| 2026-10-29 | OPT-2: 판정 결과에 '안전'이 하나도 없으면 "다른 식당 보기"를 보여 주고, 팀이 조사한 학교 주변 음식점 시드에서 사용자 제한을 모두 피하는 곳을 추천한다 | "학식을 이용할 수 없는 경우"와 데이터 출처가 비어 있었다 | 반영: `problem.yaml > features > OPT-2`, `api.md > OPT-2`. 음식점 조사는 팀이 한다 |
| 2026-10-29 | (잠정, 역할 ①) 클라이언트는 `POST /api/v1/users`로 `user_id`를 발급받아 브라우저(`localStorage`의 `user_id`)에 보관한다. 서버에 없는 번호로 저장하면 404를 받고 새로 발급받는다 | `user_id`를 서버가 Auto Increment로 발급하기로 해서 발급 방법이 필요했다 | 반영: `api.md` REQ-4 사용자 번호 발급, `erd.md` 열린 질문 2 |
| 2026-10-29 | (잠정, 역할 ①) `docker compose`의 `app`은 Next.js(3000)다. 브라우저는 같은 출처 `/api/v1/**`을 부르고 Next.js가 `BACKEND_URL`로 백엔드에 넘긴다. API 기본 경로는 `/api/v1` | 화면과 백엔드를 나눠도 키트 조건(`app` 하나가 3000·`/__health`)을 지키고 CORS를 피한다 | 반영: `tech-stack.md` 열린 결정 1·2, `folder-structure.md`, `api.md` 공통, `docker-compose.yml` |
| 2026-10-29 | (잠정, 역할 ①) 백엔드 뼈대는 Java 21 + Spring Boot 4.1.2 + Gradle 9.8.2 + Spring AI 2.0.1, 기본 패키지 `com.nullteam.foodcheck`. 시드는 Flyway 반복 마이그레이션(`R__`)으로 역할별 파일에 쓴다 | 뼈대를 만들 때 정해야 했다. 팀이 다른 값을 고르면 바꾼다 | 반영: `tech-stack.md`, `folder-structure.md`, `roles.md` |
| 2026-10-29 | 음식 DB에 없는 메뉴는 만개의 레시피에서 재료를 검색해 판정한다. 이 경로는 흐름 `manual-input-unknown-menu`로 확인한다 | 팀 시나리오 5번의 "DB에 없으면 팝업"을 대신한다 | 반영: `problem.yaml > features > REQ-5`, `scenarios.yaml`. `roles.md`와의 충돌은 위 E2E 결정으로 해소 |
| 2026-10-29 | 사용자 식별자는 `user_id`로 통일한다. 회원가입·로그인 없이 서버가 Auto Increment로 발급한다 | 회원을 두지 않기로 했다. 앞서 정한 `session_id`를 대신한다 | 반영: `problem.yaml > features > OPT-2`, `scenarios.yaml`, `erd.md`, `api.md`, `roles.md`. 처음 발급 방법은 `erd.md > 열린 질문` 2번 |
| 2026-10-29 | 백엔드는 `domain/<도메인>/{controller, service, dto, entity, repository}`, 프런트엔드는 레이어드 방식으로 폴더를 나눈다 | 팀이 폴더 구조의 기본을 정했다 | 반영: `folder-structure.md`. 그 아래 세부는 제안이고 기본 패키지 이름은 미정이다 |
| 2026-10-29 | 기술 스택은 작업 중 바뀔 수 있다. 바뀌면 같은 커밋에서 팀 협업 문서를 함께 고치고, 확정되면 `tech-stack.md`의 `미정`을 지운다 | 해커톤 중 실시간으로 바뀌는 선택이 많아, 문서와 코드가 따로 가면 어긋난다 | 반영: `ai-rules.md` 5번, `git-convention.md` 커밋 규칙 |
| 2026-10-29 | 백엔드는 Spring Boot, 화면은 Next.js로 나눈다. DB 접근은 JPA + Flyway, AI는 Spring AI, 검증은 Bean Validation, 판정 엔진 테스트는 JUnit 5를 쓴다 | 팀이 백엔드를 Spring Boot로 정했다. 노션 기술 스택 표의 Drizzle·Zod·Vercel AI SDK·Vitest는 TypeScript 서버 전제라 대체한다 | 반영: `tech-stack.md`, `CLAUDE.md`, `erd.md`. 노션 원본은 아직 안 바뀌었다. `app` 서비스를 어느 쪽으로 두는지 등 세부는 `tech-stack.md > 열린 결정` |
