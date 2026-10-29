# 우리 팀 진행 기록

Agent가 서비스 저장소 루트의 `progress.md`로 복사해 이어 쓴다(`docs/` 아래에 두지 않는다). 필수 제출 양식을 대신하는 문서가 아니다. 비밀번호·토큰·팀 로그인 코드는 적지 않는다.

## 현재 상태

- 실제 서비스 저장소 폴더: `C:\Users\a4710\NULL-SUMTECH` (안내 폴더: `C:\Users\a4710\loopthon-starter`)
- 현재 단계 / 다음 행동: 2단계 기획 문서 통일 완료(2026-10-29, `docs/decisions.md`). ①의 뼈대와 REQ-4·OPT-2는 `main`에 올라왔다. 다음은 ②·③이 자기 dto와 `src/types` 파일부터 시작
- 학생에게 확인할 내용: E1이 언제 겪은 일인지, E2가 누구의 경험인지, 배포 대상(`docs/tech-stack.md > 열린 결정` 1)
- 참고: 운영진의 검색·AI 사용 허락 받음(학생 확인, 2026-10-29). 조직 이름 `NULL`·`NULL-team`은 이미 다른 계정이 사용 중이고, `NULL-sumtech-team` 조직은 GitHub에서 확인되지 않아 학생 요청으로 개인 계정 레포를 사용함

## 1. GitHub 준비

- [x] 기존 저장소·브랜치·remote와 로그인 확인 (gh 계정: lyudongwon)
- [x] 학생과 저장소 이름 합의 (답을 받기 전에는 생성하지 않음) → NULL-SUMTECH
- [x] 팀의 private 레포 생성 또는 기존 레포 확인
- [x] loopthon-bot 앱을 팀 레포에 설치 (https://github.com/apps/loopthon-bot/installations/new, 팀 레포만 선택)
- [x] 팀 코드로 대시보드 API 로그인(`data.teamId`), 레포 URL을 HTTP로 저장(`data.team.repoUrl`) 확인. 팀 코드로는 화면에 들어갈 수 없음
- [x] 사이트에 등록한 레포를 실제 작업 폴더로 clone, remote·기본 브랜치 확인
- [x] .gitignore / .env.example / README 준비(`.env`·`node_modules/`·`.next/`·`dist/`·`build/` 제외, 큰 데이터 파일 커밋 안 함. 파일이 너무 많으면 저장소를 읽지 못함)
- [x] 준비 파일 commit/push 확인 (f15cb12)
- [x] 앱 설치 후 읽기 권한 확인 (5분 자동 재확인, 즉시 확인은 같은 레포 URL 다시 저장)
- 저장소 URL / 기본 브랜치 / commit SHA: https://github.com/lyudongwon/NULL-SUMTECH / main / f15cb12f05dd6b52d69644c74fe09438ddfdabee
- 사이트 등록 결과 / loopthon-bot 설치·접근 상태: teamId 9, repoUrl 저장 ok. check.result = ok, repoAccessOk = true (loopthon-bot 설치 확인, 2026-10-29)

## 2. 문제정의 기록 → 문제 다지기 → 해법 찾기 → 기능 명세

- [x] 종이 기록 수령(학생이 채팅에 옮겨 적거나 사진 첨부) — 사진 2장 + 읽기 어려운 곳(E1, WHY5, 통찰 관찰)은 학생이 채팅으로 정정
- [x] problem.example.yaml을 참고해 docs/problem.yaml 작성 (59f80f9)
- [x] 원문 의도와 불명확한 내용 확인, 없는 근거는 빈칸 유지 — 빈칸: E1 when·quote·누구 경험인지, why_ours 3개, sources, features
- [ ] 2-A 문제 다지기: 캔버스 칸을 질문으로 다짐(Agent가 해법·후보·예시 답을 먼저 내지 않음, 학생 답이 없는 칸은 "학생 답 대기")
- [ ] 팀원 경험을 언제·어떤 장면·어떻게 대응했는지까지 experiences에 기록
- [ ] 외부 자료는 열리는 URL을 sources에 그대로 적고 원문이 주장을 직접 뒷받침하는지 확인, 출처 없는 수치 없음
- [ ] 문제가 '우리 기능이 없어서'가 아니라 실제 상황·행동으로 서술됐는지 확인
- [ ] 2-B 해법 찾기: 학생이 낸 해법과 결정을 solution_paths에, 기존 대안 대신 이 해법이어야 하는 이유를 학생 말로 why_ours에 작성
- [x] 2-C features에 학생이 고른 해법의 기능별 입력·결과·완료 조건 작성 (REQ-1~7, OPT-1~3)
- [ ] YAML 문법 검사, 등록된 레포의 기본 브랜치에 docs/problem.yaml push
- [x] 학생이 개인 로그인으로 우리 팀 → 문제 정의를 열어 캔버스 표시 확인 (2026-10-29 학생 확인)(Agent는 화면을 열 수 없음, 약 1분 캐시)
- [ ] 핵심 사용자 흐름·기능별 동작 규칙·완료 조건·제외 범위를 팀과 확인
- [x] 로그인 필요 여부와 역할 이름(흐름 account·docker 시드 계정 역할 이름으로 씀) 결정 — 로그인 없음, `user_id` 발급
- [ ] 첫 화면에서 알려 줄 것(로그인하지 않은 사람이 `/`에서 볼 서비스 이름·하는 일)을 학생과 정함
- [x] 주 사용 플랫폼(desktop·mobile·both)과 실제 사용 장면에 근거한 platformReason 작성 — mobile
- [x] 사용자 핵심 흐름 3~8개를 scenarios.example.yaml 참고해 docs/scenarios.yaml에 작성(steps는 화면의 메뉴·버튼 이름으로 한 줄에 한 동작, 로그인 흐름은 account 지정), YAML 파서로 형식 확인 후 기본 브랜치 push
- [x] 서비스의 AI 호출 필요 여부 확인 — 필요(메뉴 판독 REQ-2, 재료 AI 추정 REQ-5)
- [ ] README 등 기존 문서에 확인된 구현 설계 기록, 기본 브랜치 push
- 문제정의·구현 설계 링크 / commit SHA / 확인할 빈칸:

## 3. 최소 화면 docker 실행·CI·첫 배포

- [ ] 설계에 맞는 최소 화면과 서버 실행 구성 준비, Dockerfile 작성(빌드는 Dockerfile에서 끝내고 시작 명령은 서버 실행만)
- [ ] docker-compose.example.yml을 docker-compose.yml로 복사: app·db, app만 ports "3000:3000", 앱은 컨테이너 안 3000에서 받음, 앱 설정에 localhost:3000 같은 절대 주소 없음
- [ ] app 말고는 ports·container_name이 없고, 어느 서비스에도 env_file 없음
- [ ] 환경변수 이름 그대로(RELEASE_ID·SEED_USER_*·AI_GATEWAY_API_KEY), 시드 계정 값은 `${}` 없이 직접 적음
- [ ] 빈 DB에서 `docker compose up --build`만으로 앱·시드 데이터가 2분 안에 뜸(docker를 못 쓰면 GitHub Actions 결과로 확인)
- [ ] README 설치·빌드·실행 안내와 환경변수 표(이름·용도·설정 위치)
- [ ] GET /__health가 인증·리다이렉트 없이 200 응답(docker 앱·배포 둘 다, Next.js는 app/%5F%5Fhealth), 실제 배포 commit을 releaseId로 제공
- [ ] 공개 HTTPS URL 첫 배포(발표용, 배포용 DB는 학생 확인 후 무료 등급)
- [ ] CI 실제 검사(workflows/scenarios.yml 복사)와 성공 뒤 자동 배포 연결, 실제 push로 확인
- [ ] Agent가 대시보드 배포 URL 저장, UP·배포 commit 일치 확인
- 배포 URL / CI·배포 링크 / 첫 배포 commit SHA:

## 4. 서비스 AI API 설정 (필요할 때만)

- 해당 여부 / 선택한 Gateway 지원 모델:
- [ ] 모든 지원 모델을 자유롭게 사용하며 권장 목록은 추천용임을 확인
- [ ] 팀 전체 누적 $10 한도와 현재 사용액·남은 예산·선택 모델 단가 확인
- [ ] 학생이 자료 → API 키에서 키가 없으면 직접 발급 후 복사, Agent는 키를 발급·조회하지 않음
- [ ] 서버가 환경변수 AI_GATEWAY_API_KEY에서 키를 읽음(주최 측 실행도 이 이름으로 키를 넣음), docker-compose.yml app에 `AI_GATEWAY_API_KEY: ${AI_GATEWAY_API_KEY:-}`
- [ ] 학생이 AI_GATEWAY_API_KEY를 로컬·배포 환경과 GitHub Secrets에 직접 설정, .env.example에는 이름만 기록
- [ ] 서버에서 Gateway 호출, 접근 제한·예산 부족·지연·실패 처리
- [ ] 팀 예산 안의 작은 실제 요청으로 연결 확인
- 키 값을 제외한 설정·연결 확인 결과 / 준비 대기 사항:

## 5. 핵심 기능 구현

- [ ] (로그인이 있으면) 회원가입·로그인·로그아웃·역할별 권한 검사 구현, 아이디/비밀번호로 바로 로그인 가능
- [ ] 로그인하지 않은 사람이 `/`를 열면 서비스 이름과 하는 일이 보임(`/`를 로그인 화면으로 보내거나 로그인 칸만 두지 않음)
- [ ] 핵심 입력 → 실제 처리 → 결과 → 다음 행동 구현
- [ ] features의 동작 규칙·완료 조건과 대조, 새 기능은 features에 먼저 추가
- [ ] 화면 다듬기(문구·배치·빈 화면 안내·시각)는 학생이 정한 방향 안에서 Agent가 만들어 보여 주고 학생이 고름
- [ ] docs/scenarios.yaml의 메뉴·버튼 이름이 실제 화면과 같은지 확인, 화면 이름을 바꾸면 흐름·spec도 같은 commit에서 수정
- [ ] 로그인 전에는 로그인 필요 화면이 열리지 않음, 없는 주소는 404(500 아님), 주 사용 플랫폼 화면 폭에서 깨지지 않음
- [ ] 실패 처리와 의미 있는 테스트, 실행한 검사 결과 기록
- [ ] 의미 있는 변경 commit/push, CI·자동 배포 결과 확인
- 완료한 과업 / 검사 결과 / 남은 구현:

## 6. 흐름 E2E

- [ ] 키트 e2e/를 저장소 e2e/로 복사, platform에 맞는 project만 남김
- [ ] 흐름마다 e2e/scenarios/<id>.spec.ts 하나(id와 같은 파일 이름), 파일마다 test 하나
- [ ] steps를 같은 순서의 화면 조작으로 spec 본문에 옮김(API 호출 없음), 다른 역할 단계는 새 창(context)으로 로그인, 회원가입 이메일은 실행마다 다르게
- [ ] page.goto는 창마다 처음 한 번만(로그인 화면으로 가는 goto만 예외), 회원가입은 첫 화면에서 눌러 가거나 회원가입 주소 하나만, 도우미 함수는 test보다 위에
- [ ] 마지막에 expected 결과를 expect로 확인(화면이 열렸는지만 보지 않음)
- [ ] test.skip·test.fixme·test.only·조건부 return·실패를 삼키는 try/catch 없음
- [ ] page.route·route.fulfill·routeFromHAR·routeWebSocket·addInitScript·화면 글자를 바꾸는 page.evaluate 없음
- [ ] 흐름에 필요한 선행 데이터는 앱 시드로, 업로드 자료는 e2e/fixtures/에 커밋, 앞 흐름이 만든 데이터에 기대지 않음
- [ ] AI 기능은 실제 응답을 기다려 확인, 흐름 하나가 로컬에서 1분 안
- [ ] viewport를 spec에서 바꾸지 않음, platform의 화면(both면 둘 다)에서 통과
- [ ] globalSetup·storageState 등 playwright.config.ts에만 있는 설정에 기대지 않음
- [ ] `docker compose down -v`, `docker compose up -d --build` 뒤 `npx playwright test`로 모든 흐름 통과
- 흐름별 E2E 결과(통과 / 실패 단계와 이유):

## 7. 최종 검증

- [ ] 실제 배포에서 인증·핵심 과업·대표 실패 동작과 환경변수 확인
- [ ] 빈 DB의 docker 앱에서 platform 화면으로 모든 흐름 E2E 통과, spec이 steps·expected 그대로인지 다시 읽음, 프로젝트 검사, 비밀값 노출 확인
- [ ] 최종 기본 브랜치 push, 해당 commit의 CI 성공·자동 배포 확인
- [ ] 같은 배포 URL을 다시 저장해 대시보드 UP·최종 배포 commit 일치 확인
- [ ] 학생이 자료 → 제출 요건 화면에서 노란 줄이 없다고 확인
- [ ] 문제정의·구현 설계·최종 결과 링크와 미확인 사항 보고
- 검사 결과 / CI·배포 링크 / 최종 commit SHA:
- 흐름별 E2E 결과(통과 / 실패 단계와 이유):
- 배포 URL:
- 아직 못 한 일 / 제한 / 다음 행동:

## 문서 다듬기(랄프 루프)

- 1회차(2026-10-30) `docs/problem.yaml`: 머리 주석·features 주석을 짧게 묶고, context·measurable·REQ-4 input·REQ-5 output·REQ-6 done 문장을 자연스럽게 고침. 기능 ID·화면 문구·출처·수치·결정 내용은 그대로
  - 검사: `yaml.safe_load` 통과(features ID 11개 그대로), `git diff`로 바뀐 줄의 ID(REQ/OPT/R/E) 짝이 맞는지 확인
- 2회차(2026-10-30) `README.md`: 첫머리에 문제 → 누가 겪는지 → 해법 → 핵심 기능 소개 3문단을 problem.yaml 내용만으로 넣고, 나열식 "~한다" 문장을 읽기 쉬운 말투로 고침. 명령·포트·환경변수 표·경로는 그대로
  - 검사: 고치기 전후 README의 백틱 토큰·`.md` 링크·URL 목록을 뽑아 비교해 똑같음을 확인, 새 링크 `docs/problem.yaml`은 실제 파일
- 3회차(2026-10-30) `docs/decisions.md`: 머리말을 읽기 쉽게 고치고, 굵은 글씨 2곳을 빼고, 이유 칸의 딱딱한 문장 8곳을 자연스럽게 고침. 결정 21행·날짜·반영 상태·결정 내용은 그대로
  - 검사: 고치기 전후 백틱 토큰·`.md` 링크·REQ/OPT/R 번호 목록을 뽑아 `diff`로 비교해 똑같음, 결정 행 수 21 그대로, `git diff`로 바뀐 9줄을 다시 읽음
- 4회차(2026-10-30) `docs/api.md`: 머리말을 짧게 고치고, 굵은 글씨 10곳(판독·추론 순서·열린 질문 제목 등)을 빼고, 설명 문장 몇 곳의 말투를 다듬음. 경로·필드·열거형·예시 JSON·영어 화면 문구·에러 `detail`·판정 규칙 표는 그대로
  - 검사: 고치기 전후 백틱 토큰·링크·영어 따옴표 문구·REQ/OPT/R 번호 목록을 뽑아 `diff`로 비교함. 다른 점은 `matched_restrictions` 설명에 `CONFIRMED` 하나를 넣은 것뿐(뜻은 그대로). `git diff`로 바뀐 14줄을 다시 읽음
- 5회차(2026-10-30) `docs/scenarios.yaml`: 머리 주석 11줄과 platformReason 말투만 다듬음. 시나리오 id·title·features·steps·expected는 손대지 않음
  - 검사: `yaml.safe_load` 통과(시나리오 7개), HEAD와 파싱 결과 비교해 `scenarios`·`platform`·`login` 값이 똑같음, 주석의 REQ/OPT·F번호 대응·파일 경로 토큰 목록 `diff` 동일
- 6회차(2026-10-30) `docs/erd.md`: 머리말·전제·판정 설명 문장을 다듬고, 굵은 글씨 22곳(목록 머리말·열린 질문 제목)을 뺌. 다이어그램·테이블·컬럼·시드 요구·열린 질문 내용은 그대로
  - 검사: 고치기 전후 백틱 토큰·링크·REQ/OPT/R/E 번호·따옴표 문구 목록을 뽑아 `diff`로 비교해 똑같음, `git diff --word-diff`로 바뀐 줄을 다시 읽음. "AI가 아니라 코드가 판정"은 tech-stack.md·problem.yaml에 있는 내용
- 7회차(2026-10-30) `docs/tech-stack.md`: 머리말·실행 조건·열린 결정 문장을 읽기 쉬운 말투로 고치고 굵은 글씨 13곳을 뺌. 버전·표 13행·환경변수·명령·경로·열린 결정 번호 1~6은 그대로
  - 검사: 고치기 전후 백틱 토큰·링크·REQ/OPT/R 번호·용량·따옴표 문구 목록을 `diff`로 비교함. 다른 점은 열린 결정 1에서 "Next.js가 `app`이다"를 "잠정으로 Next.js입니다"로 줄여 `app` 토큰 하나가 빠진 것뿐(뜻은 그대로). `git diff --word-diff`로 바뀐 줄을 다시 읽음
  - 발견: `docs/api.md` 열린 질문 2가 AI 호출 남용을 "tech-stack.md 열린 결정 2"로 가리키지만 실제로는 4번. 이번엔 고치지 않음
- 8회차(2026-10-30) `docs/folder-structure.md`: 머리말·백엔드·프런트엔드 설명 문장을 다듬고 굵은 글씨 9곳을 뺌((제안) 표시는 그대로 둠). 폴더 트리·경로·규칙 표·열린 질문은 그대로. 7회차에 발견한 `docs/api.md` 열린 질문 2의 참조를 "tech-stack.md 열린 결정 2"에서 4로 바로잡음
  - 검사: 고치기 전후 백틱 토큰·링크·REQ/OPT 번호·(제안) 표시 목록을 `diff`로 비교해 똑같음, `git diff --word-diff`로 바뀐 줄을 다시 읽음. tech-stack.md 열린 결정 4가 "AI 호출 남용 방지"임을 확인함
- 남은 문서: 없음. 다듬기 대상(problem.yaml, README.md, decisions.md, api.md, scenarios.yaml, erd.md, tech-stack.md, folder-structure.md)을 모두 마침. ai-rules.md·git-convention.md·roles.md는 작업 규칙이라 손대지 않음
- 다음 행동: 팀원이 `chore/ralph-loop` 변경을 읽어 보고 main으로 PR할지 정함

## 기획 검토(랄프 루프)

10년차 서비스 기획자 관점의 검토. 이미 정해진 뜻을 분명히 쓰거나 문서끼리 어긋난 곳은 직접 고치고, 기능·한도·판정 규칙·API·화면 이름을 바꾸는 일은 아래 "팀 결정 필요"에 적는다.

- [x] A. `problem.yaml > features`: 입력·입력 조건·완료 조건·예외 처리, 관찰 가능한 완료 조건, 구현 방식 섞임
- [ ] B. 문서끼리 일치(problem.yaml ↔ scenarios.yaml ↔ api.md ↔ decisions.md ↔ erd.md)
- [ ] C. 판정 기준 R0~R4와 프로필이 비었을 때·판독 실패 시 동작
- [ ] D. 해커톤 범위(MVP REQ / 부가 OPT 구분과 이유, 시연 흐름)
- [ ] E. 문제 정의·성공 지표
- [ ] F. 개인정보·윤리(알레르기·종교 정보, 사진 저장 동의·보관)

### 회차 기록

- 1회차(2026-10-30) A 검토: 11개 기능 모두 input·output·done은 있으나, 입력 조건과 예외가 `api.md`에만 있고 `done`에 빠진 곳이 많았다. `api.md`·`decisions.md`에서 이미 정한 내용만 `done`에 옮겨 적었다.
  - 고친 것(`problem.yaml > features`): REQ-1에 형식·10MB 한도와 거절 시 문구, REQ-2에 판독 실패·45초 초과 시 입력칸, REQ-3에 식단 제한 미저장 시 R0(안전), REQ-4에 다시 열었을 때 보이는 상태와 동시 저장 시 보이는 문구(요구사항 자리의 "version 불일치 → 409"를 화면 결과로 바꿈), REQ-5에 재료를 못 찾았을 때, REQ-6의 "따로 요청하지 않는다"(구현 방식)를 화면 결과로 바꿈, OPT-1에 빈 입력 팝업
  - 그대로 둔 것: 필드 구조(input·output·done). `input_condition`·`exceptions` 키를 새로 두면 대시보드 캔버스 표시가 바뀔 수 있어 `done` 안에 적었다
  - 검사: `yaml.safe_load` 통과(features ID 11개 그대로), `git diff --word-diff`로 `done` 7곳만 바뀐 것을 확인. scenarios.yaml·api.md는 손대지 않음
  - 다음: B

### 팀 결정 필요

- (A, REQ-2) 학식 식판처럼 음식 사진 한 장에 요리가 여러 개 있을 때 동작이 없다. 지금 규칙은 "요리명 1개"뿐이라 나머지 요리는 판정되지 않는다. 제안: 시연 범위에서는 한 그릇 사진만 다룬다고 `REQ-2 input`에 적을지 팀이 정한다
- (A, REQ-1·OPT-3) 10MB 한도를 지키는 수단이 부가 기능 OPT-3(축소)이다. 휴대폰 원본 사진은 10MB를 넘을 수 있어, OPT-3을 빼면 MVP 업로드가 막힐 수 있다. 제안: OPT-3을 REQ로 올리거나 REQ-1 done에 "휴대폰 카메라 원본도 올라간다"를 넣을지 정한다(D에서 다시 본다)
- (A, REQ-2·OPT-1) MVP인 REQ-2의 실패 처리가 부가 기능 OPT-1(수동 입력)에 기댄다. 제안: OPT-1을 REQ로 올리거나, OPT-1이 없을 때 REQ-2 실패 화면을 정한다(D에서 다시 본다)
- (A, REQ-3) 식단 제한을 저장하지 않은 사용자는 모든 메뉴가 "Safe"로 나온다(R0). 설정을 잊은 알레르기 사용자가 "Safe"를 믿을 위험이 있다. 제안: 제한이 없을 때 판정 화면에 설정 안내를 보여 줄지 정한다(C에서 다시 본다)
- (A, OPT-2) 식단 제한이 없는 사용자가 첫 화면의 "Other restaurants"를 누를 때 어떤 음식점이 보이는지 정해져 있지 않다. 제안: 시드 음식점 전부를 보여 줄지 정한다
- (A, 형식) features에 입력 조건·예외를 따로 적을 칸이 없어 `done` 문장이 길다. 제안: `input_condition`·`exceptions` 키를 둘지 대시보드 표시를 확인한 뒤 정한다
