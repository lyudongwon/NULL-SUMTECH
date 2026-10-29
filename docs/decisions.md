# 결정 기록

채팅이나 회의에서 정한 결정을 한 줄로 남긴다. 최신 항목을 위에 둔다. 결정이 문서에 반영되면 "반영 상태"를 바꾼다. 규칙은 [ai-rules.md](ai-rules.md) 4번을 따른다.

| 날짜 | 결정 | 이유 | 반영 상태 |
| --- | --- | --- | --- |
| 2026-10-29 | **문서 통일.** 원본은 레포(`problem.yaml` > `scenarios.yaml` > `api.md` 순)이고, 노션 통합 페이지는 레포를 옮겨 적는 읽기용이다 | 노션·레포 문서가 서로 어긋났다. 세 세션의 Claude가 읽는 곳이 레포다 | 반영: 전 문서, `api.md` 머리말 |
| 2026-10-29 | 기술 스택은 **Next.js 하나(화면 + API Route Handler) + Drizzle + PostgreSQL + Vercel AI SDK**로 한다. 아래 Spring Boot 결정을 대신한다 | `feat/base-settings`가 이미 Next.js로 만들어졌고, 대회 실행 조건(3000 포트 `app` 하나, 2분 안에 기동)과 배포 하나에 맞는다. 분리하면 CORS·배포 2곳·JVM 기동 시간이 생긴다 | 반영: `tech-stack.md`, `folder-structure.md`, `roles.md`, `erd.md`, `CLAUDE.md` |
| 2026-10-29 | 음식 DB에 없는 메뉴는 이름·조리법 → 만개의 레시피 → AI 추정 순으로 재료를 찾는다. AI 추정만으로는 '확인 필요'를 넘지 않는다. '등록되지 않은 메뉴' 404·팝업은 없앤다. 찾은 재료는 `foods`에 저장한다 | 외부 사이트가 막혀도 판정이 멈추지 않게 한다. 시나리오의 "팝업 없이 판정"과 맞춘다 | 반영: `problem.yaml > features > REQ-5`, `api.md > REQ-3·OPT-1·REQ-5`, `erd.md` |
| 2026-10-29 | E2E 메뉴는 시드에 넣는다. 예외는 `manual-input-unknown-menu`의 된장찌개 하나다 | DB에 없는 경로를 확인해야 하고, AI 추정 덕분에 외부 사이트 없이도 배지가 나온다 | 반영: `roles.md`, `erd.md`, `scenarios.yaml`. 아래 "충돌해 확인 필요"를 해소한다 |
| 2026-10-29 | 프리셋은 `HALAL`(돼지고기·술)과 `VEGETARIAN`(채식: 고기·해산물, 계란·유제품 허용) 2개다. 알레르기는 토글(`egg`·`dairy`·`peanut`·`seafood`)로 고른다. 제한 항목은 `meat`·`pork`·`beef`·`seafood`·`egg`·`dairy`·`peanut`·`alcohol`이고 `pork`·`beef`는 `meat` 아래다 | 시나리오의 "채식"·"할랄"·"고기"와 코드를 맞춘다 | 반영: `api.md > 열거형`, `erd.md`, `problem.yaml > features > REQ-4`. `feat/base-settings` 시드는 ①이 고친다 |
| 2026-10-29 | 판정 규칙은 R0(제한 없음 → 안전), R1(확인된 재료가 걸림 → 불가), R2(추정 재료가 걸림 → 확인 필요), R3(재료가 모두 AI 추정 → 확인 필요), R4(걸림 없음 → 안전)이다. 판정 응답에 REQ-6 근거 상세(`rule`, 재료별 `evidence`·`confidence`·`basis`)를 함께 담는다 | REQ-6 필드가 API에 없었다. 추가 요청 없이 상세를 보여 준다 | 반영: `api.md > REQ-3` |
| 2026-10-29 | `user_id`는 `POST /api/v1/users`로 발급받아 `localStorage`에 둔다. 모든 API는 `/api/v1`로 시작한다(`/__health` 제외). 업로드 한도는 10MB. `user_id`만 알면 남의 설정을 볼 수 있는 점은 해커톤 범위에서 받아들인다 | 발급 방법·기본 경로·한도가 비어 있었다 | 반영: `api.md`, `erd.md`. `feat/base-settings`의 uuid는 ①이 serial로 고친다 |
| 2026-10-29 | 빈 메뉴 처리: 판독 결과가 전부 비었거나 422·500이면 메뉴 이름 입력칸(OPT-1)을 보여 주고, 판정 요청의 이름이 비었으면 '입력 값이 없습니다.' 팝업을 띄운다 | `problem.yaml`, `api.md`, 시나리오가 서로 달랐다 | 반영: `problem.yaml > features > REQ-2·REQ-3`, `api.md > REQ-2·REQ-3` |
| 2026-10-29 | OPT-2: 판정 결과에 '안전'이 하나도 없으면 "다른 식당 보기"를 보여 주고, 팀이 조사한 학교 주변 음식점 시드에서 사용자 제한을 모두 피하는 곳을 추천한다 | "학식을 이용할 수 없는 경우"와 데이터 출처가 비어 있었다 | 반영: `problem.yaml > features > OPT-2`, `api.md > OPT-2`. 음식점 조사는 팀이 한다 |
| 2026-10-29 | 음식 DB에 없는 메뉴는 만개의 레시피에서 재료를 검색해 판정한다. 이 경로는 흐름 `manual-input-unknown-menu`로 확인한다 | 팀 시나리오 5번의 "DB에 없으면 팝업"을 대신한다 | 반영: `problem.yaml > features > REQ-5`, `scenarios.yaml`. `roles.md`와의 충돌은 위 E2E 결정으로 해소 |
| 2026-10-29 | 사용자 식별자는 `user_id`로 통일한다. 회원가입·로그인 없이 서버가 Auto Increment로 발급한다 | 회원을 두지 않기로 했다. 앞서 정한 `session_id`를 대신한다 | 반영: `problem.yaml > features > OPT-2`, `scenarios.yaml`, `erd.md`, `api.md`, `roles.md`. 처음 발급 방법은 `erd.md > 열린 질문` 2번 |
| 2026-10-29 | 백엔드는 `domain/<도메인>/{controller, service, dto, entity, repository}`, 프런트엔드는 레이어드 방식으로 폴더를 나눈다 | 팀이 폴더 구조의 기본을 정했다 | 반영: `folder-structure.md`. Next.js 기준으로 계층을 옮겨 적었다 |
| 2026-10-29 | 기술 스택은 작업 중 바뀔 수 있다. 바뀌면 같은 커밋에서 팀 협업 문서를 함께 고치고, 확정되면 `tech-stack.md`의 `미정`을 지운다 | 해커톤 중 실시간으로 바뀌는 선택이 많아, 문서와 코드가 따로 가면 어긋난다 | 반영: `ai-rules.md` 5번, `git-convention.md` 커밋 규칙 |
| 2026-10-29 | 백엔드는 Spring Boot, 화면은 Next.js로 나눈다. DB 접근은 JPA + Flyway, AI는 Spring AI, 검증은 Bean Validation, 판정 엔진 테스트는 JUnit 5를 쓴다 | 팀이 백엔드를 Spring Boot로 정했다. 노션 기술 스택 표의 Drizzle·Zod·Vercel AI SDK·Vitest는 TypeScript 서버 전제라 대체한다 | **대체됨**: 위 "Next.js 하나" 결정 |
