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
- [x] B. 문서끼리 일치(problem.yaml ↔ scenarios.yaml ↔ api.md ↔ decisions.md ↔ erd.md)
- [x] C. 판정 기준 R0~R4와 프로필이 비었을 때·판독 실패 시 동작
- [x] D. 해커톤 범위(MVP REQ / 부가 OPT 구분과 이유, 시연 흐름)
- [x] E. 문제 정의·성공 지표
- [x] F. 개인정보·윤리(알레르기·종교 정보, 사진 저장 동의·보관)

### 회차 기록

- 1회차(2026-10-30) A 검토: 11개 기능 모두 input·output·done은 있으나, 입력 조건과 예외가 `api.md`에만 있고 `done`에 빠진 곳이 많았다. `api.md`·`decisions.md`에서 이미 정한 내용만 `done`에 옮겨 적었다.
  - 고친 것(`problem.yaml > features`): REQ-1에 형식·10MB 한도와 거절 시 문구, REQ-2에 판독 실패·45초 초과 시 입력칸, REQ-3에 식단 제한 미저장 시 R0(안전), REQ-4에 다시 열었을 때 보이는 상태와 동시 저장 시 보이는 문구(요구사항 자리의 "version 불일치 → 409"를 화면 결과로 바꿈), REQ-5에 재료를 못 찾았을 때, REQ-6의 "따로 요청하지 않는다"(구현 방식)를 화면 결과로 바꿈, OPT-1에 빈 입력 팝업
  - 그대로 둔 것: 필드 구조(input·output·done). `input_condition`·`exceptions` 키를 새로 두면 대시보드 캔버스 표시가 바뀔 수 있어 `done` 안에 적었다
  - 검사: `yaml.safe_load` 통과(features ID 11개 그대로), `git diff --word-diff`로 `done` 7곳만 바뀐 것을 확인. scenarios.yaml·api.md는 손대지 않음
  - 다음: B
- 2회차(2026-10-30) B 검토: 기능 ID·판정 단계 이름(Safe·Check first·Avoid)·열거형·한도(10MB, 45초, 1400px·0.85, 최대 30개)는 다섯 문서에서 같았다. 어긋난 곳은 problem.yaml·api.md 쪽 뜻에 맞춰 고쳤다.
  - 고친 것: `api.md > OPT-2`·`erd.md > 음식점 추천`이 "Other restaurants"를 판정 화면에서만 부른다고 적어 첫 화면 버튼(problem.yaml OPT-2, decisions 2026-10-29)이 빠져 있던 것. `erd.md > 사진`이 모든 사진을 1400px로 줄인다고 적은 것(1400px 미만은 원본, OPT-3). `erd.md > 음식 DB`가 REQ-5 찾는 순서의 2단계(이름 속 재료 단어·조리법)와 시드의 숨은 재료(`INFERRED`·`COOKING_METHOD`)를 빠뜨린 것. `erd.md` 열린 질문 1·`api.md` 열린 질문 4가 ②의 잠정 저장 방식(메모리 → `bytea`)을 가리키지 않던 것. `erd.md` 열린 질문 4의 "전체 목록이 필요하다"(이미 확정). `api.md` 열거형 `CONFIRMED` 설명(DB에는 추정 재료도 저장되므로 "시드에 확인된 재료로 등록된 재료"로)
  - `decisions.md`: 결정 문장은 기록이라 그대로 두고, 뒤 결정으로 바뀐 한국어 팝업·"다른 식당 보기"·`영어 이름 (한국어 원문)` 표기를 "반영 상태" 칸에 적었다
  - 그대로 둔 것: scenarios.yaml(어긋남 없음), 기능 ID·API 경로·필드·열거형 값·화면 문구
  - 검사: `git diff`로 scenarios.yaml·problem.yaml이 바뀌지 않았고 API 경로·열거형 값이 그대로임을 확인
  - 다음: C
- 3회차(2026-10-30) C 검토: R0~R4의 뜻은 problem.yaml·scenarios.yaml·api.md·erd.md·decisions.md와 구현(`backend/.../JudgmentRules.java`)에서 같았다. 시나리오 기대 결과(설렁탕 R1 "Avoid", 비빔밥·비빔국수 R2 "Check first")도 규칙과 맞는다. 프로필이 비었을 때는 R0(Safe), 판독 실패는 메뉴 이름 입력칸(OPT-1)으로 정해져 있다.
  - 고친 것: `api.md > 판정 규칙`에 재료가 비었을 때도 `R3`이라는 것(2회차 팀 결정 항목 중 `rule` 값. "모두 AI 추정"이 빈 목록에도 참이고 구현도 `R3`), AI 추정만 있으면 제한이 있는 사용자에게 늘 `CHECK`라는 것, 예시에 없던 `R0`·`R3` `reason` 문구(구현 그대로)를 적음. `api.md > REQ-5 예외`의 "R3과 같이"를 "`R3`, 제한 없으면 `R0`"으로. `problem.yaml` REQ-5 done의 "'확인 필요'를 넘지 않는다"(Safe도 되는 것처럼 읽힘)와 `insight.design_decision`의 "'확인 필요'까지만"(Safe가 없는 것처럼 읽힘)을 규칙 뜻대로 "'불가'가 나오지 않는다"로 분명히 씀
  - 그대로 둔 것: 규칙 조건·순서, 열거형, 화면 문구 표, scenarios.yaml
  - 검사: `yaml.safe_load` 통과, `git diff`로 API 경로·열거형·scenarios.yaml이 그대로임을 확인
  - 다음: D
- 4회차(2026-10-30) D 검토: 주요(REQ-1~8)·부가(OPT-1~3) 구분은 주석에만 있고 나눈 이유가 없다. 부가인 OPT-1·OPT-3은 이미 만들어졌고(`ManualMenuInput.tsx`·`resizeImage.ts`), MVP 흐름이 기댄다(REQ-2 실패 → OPT-1 입력칸, 배포 프록시 4.5MB 제한 아래 REQ-1 10MB 한도 → OPT-3). 시연 장면은 E1·E2 흐름(decisions 2026-10-29)으로 정해져 있지만 순서·대체 계획은 없다. 시나리오 7개가 REQ-1~8을 모두 덮고, OPT-2만 흐름이 없다(음식점 시드 대기)
  - 고친 것(`problem.yaml`): 주요인 REQ-8이 OPT 뒤에 있던 것을 REQ-7 다음으로 옮김(내용 그대로). features 앞 주석에 "주요 REQ-1~8 / 부가 OPT-1~3"과 부가지만 MVP가 기대는 곳 두 가지를 적음(이미 문서에 있던 사실)
  - 그대로 둔 것: 기능 ID·done·REQ/OPT 표시, scenarios.yaml
  - 검사: `yaml.safe_load` 통과, features ID 11개 그대로(순서만 REQ-8 이동), `git diff`로 REQ-8 블록 내용이 같음을 확인. 코드에서 features 순서를 읽는 곳 없음
  - 다음: E
- 5회차(2026-10-30) E 검토: `statement`·`root_problem`·`five_whys`는 "기능이 없어서"가 아니라 실제 상황(메뉴판에 요리 이름만 있고 원재료 표시 의무가 없음, E1·E2 포기 경험)으로 적혀 있다. 성공 지표는 `canvas.impact.measurable` 두 줄뿐이다. "확인 시간 5분 → 15초"는 숫자는 있으나 시작·끝 시점과 측정 조건이 없고, 어느 기능 done·시나리오 expected에도 이어져 있지 않아 통과/실패를 가릴 수 없다. "포기한 끼니 한 달 약 10끼"는 지금 값만 있고 목표가 없다. 근거 인용은 R1(숫자 일치)·R2(별표 24 7개 항목)는 claim과 맞고, R3·R4를 쓴 문장 두 곳이 claim보다 넓게 말한다
  - 고친 것(`problem.yaml > five_whys`): "무슬림 유학생은 학식을 가장 적게 경험한다(R3)"를 R3 claim 그대로 "대학 급식에서 한국 음식을 경험한 비율이 가장 낮다(R3)"로 바꿈(R3는 학식 이용 빈도가 아니라 한국 음식 경험 비율을 말함)
  - 그대로 둔 것: 지표 숫자(5분·15초·10끼, 학생 제공 값. 2026-10-30 출처를 `problem.yaml > canvas > impact` 주석에 10-23 측정 기록으로 적음), R4를 쓴 "일부 학교만" 문장(아래 팀 결정 필요), features, scenarios.yaml
  - 검사: `yaml.safe_load` 통과, `git diff`로 `five_whys` 한 줄만 바뀐 것을 확인
  - 다음: F
- 6회차(2026-10-30) F 검토: 문서에 적힌 사실은 이렇다. 회원·로그인이 없고 `users`에는 번호와 생성 시각만 있다(`erd.md`). 식단 제한(알레르기·할랄·채식)은 서버 DB에 `user_id`와 함께 저장된다(REQ-4). 사진은 `user_id`와 잇지 않고 본문은 백엔드 메모리에 최대 200장·30분 둔다(`api.md > REQ-1 > 사진 본문 저장`). 판독·판정 결과는 저장하지 않는다(`erd.md`). 사진은 판독을 위해 외부 AI(AI Gateway)로 보낸다(`tech-stack.md`). 동의·고지 문구, 보관 기간 원칙, 설정 삭제 방법은 problem.yaml·scenarios.yaml·api.md·화면 코드 어디에도 없다
  - 고친 것: 없음. 빠진 것은 모두 화면 문구·API·보관 기간을 새로 정하는 일이라 아래 "팀 결정 필요"에 적음
  - 검사: `docs/`와 `frontend/src`에서 개인정보·동의·보관·삭제·privacy·consent를 찾아 위 사실 외에 정한 내용이 없음을 확인
  - 다음: A~F 모두 마침. 팀이 "팀 결정 필요" 목록을 읽고 정한다

### 팀 결정 필요

- (A, REQ-2) 학식 식판처럼 음식 사진 한 장에 요리가 여러 개 있을 때 동작이 없다. 지금 규칙은 "요리명 1개"뿐이라 나머지 요리는 판정되지 않는다. 제안: 시연 범위에서는 한 그릇 사진만 다룬다고 `REQ-2 input`에 적을지 팀이 정한다
- (A, REQ-1·OPT-3) 10MB 한도를 지키는 수단이 부가 기능 OPT-3(축소)이다. 휴대폰 원본 사진은 10MB를 넘을 수 있어, OPT-3을 빼면 MVP 업로드가 막힐 수 있다. 제안: OPT-3을 REQ로 올리거나 REQ-1 done에 "휴대폰 카메라 원본도 올라간다"를 넣을지 정한다(D에서 다시 본다)
- (A, REQ-2·OPT-1) MVP인 REQ-2의 실패 처리가 부가 기능 OPT-1(수동 입력)에 기댄다. 제안: OPT-1을 REQ로 올리거나, OPT-1이 없을 때 REQ-2 실패 화면을 정한다(D에서 다시 본다)
- (A, REQ-3) 식단 제한을 저장하지 않은 사용자는 모든 메뉴가 "Safe"로 나온다(R0). 설정을 잊은 알레르기 사용자가 "Safe"를 믿을 위험이 있다. 제안: 제한이 없을 때 판정 화면에 설정 안내를 보여 줄지 정한다(C에서 다시 본다)
- (A, OPT-2) 식단 제한이 없는 사용자가 첫 화면의 "Other restaurants"를 누를 때 어떤 음식점이 보이는지 정해져 있지 않다. 제안: 시드 음식점 전부를 보여 줄지 정한다
- (B, REQ-5) 음식 DB에 있는 메뉴에도 2단계(이름 속 재료 단어·조리법 추정)를 더하는지 정해져 있지 않다. `api.md` 찾는 순서는 3단계에만 "1에 없을 때"를 붙였고, problem.yaml REQ-5 done은 항상 쓰이는 것처럼 읽힌다. 제안: ③이 "DB에 있으면 DB 재료만" 또는 "DB 재료 + 이름 단어 추정"을 골라 `api.md > REQ-5 > 찾는 순서`에 적는다
- (B, REQ-5·REQ-3) 재료를 하나도 찾지 못했을 때 `api.md > REQ-5`는 "R3과 같이 CHECK"라 하지만, R3 조건("재료가 모두 AI_ESTIMATE")은 빈 목록을 직접 말하지 않고 응답 `rule` 값도 정해져 있지 않다(C에서 다시 본다) → 3회차에 해결: 빈 목록도 "모두 AI 추정"에 참이고 구현도 `R3`이라 `api.md`에 `R3`로 적음
- (C, REQ-3·REQ-5) 음식 DB에 없는 메뉴를 이름 단어·만개의 레시피로만 찾고 걸리는 재료가 없으면 R4(Safe)다(`api.md` OPT-1 예시 된장찌개). 일반 레시피는 학식당 레시피와 다를 수 있어, "레시피를 모르는 메뉴"에 Safe를 줄지는 안전 쪽 판단이 필요하다. 제안: 지금 규칙을 유지하고 근거 상세의 "Estimated"로 알릴지, `RECIPE_SEARCH`만 있는 경우도 R3에 넣을지 ③과 정한다(규칙 변경이라 직접 고치지 않음)
- (C, REQ-3) 식단 제한이 없을 때(R0) 구현은 근거 한 줄에 "You have no diet restrictions set."을 보인다. A에서 적은 설정 안내 항목은 이 문구로 충분한지, "Diet settings"로 가는 링크를 더할지만 남았다
- (C, REQ-3·REQ-5) 판정 API가 500을 돌려줄 때 판정 화면이 무엇을 보여 주는지(문구·다시 시도) 정해져 있지 않다. REQ-5에서 AI 추정 호출 자체가 실패했을 때 "재료를 찾지 못함"(`[]` → R3)으로 볼지 500으로 볼지도 없다. 제안: AI 실패는 `[]` → R3로 보고, 500 화면 문구를 `api.md > 화면 문구`에 더할지 ①·③이 정한다
- (A, 형식) features에 입력 조건·예외를 따로 적을 칸이 없어 `done` 문장이 길다. 제안: `input_condition`·`exceptions` 키를 둘지 대시보드 표시를 확인한 뒤 정한다
- (D) 주요·부가를 나눈 이유가 어느 문서에도 없다. 제안: "시연 흐름에 꼭 필요하면 REQ, 없어도 문제·해법을 보여 줄 수 있으면 OPT" 같은 기준 한 줄을 `problem.yaml` 주석에 팀이 적는다
- (D, OPT-1·OPT-3) 앞의 A 항목 두 개(OPT-1·OPT-3을 REQ로 올릴지)를 D에서 다시 봤다. 둘 다 이미 만들어졌고 시나리오 `manual-input-fallback`·`manual-input-unknown-menu`와 배포 업로드가 기댄다. 제안: 둘을 REQ로 올리고 ID는 그대로 둔다(ID를 바꾸면 코드·E2E 주석이 바뀐다)
- (D, 시연) 발표 시연 순서와 대체 계획이 없다. 시연은 AI 판독·만개의 레시피 같은 외부 호출에 기댄다. 제안: E1(`halal-bibimbap-gochujang`) → E2(`peanut-allergy-bibimguksu`) → 판독 실패 시 수동 입력(`manual-input-fallback`) 순서를 팀이 정하고, 네트워크가 막힐 때 시드 메뉴를 직접 입력하는 대체 장면을 정한다
- (D, OPT-2) OPT-2는 시나리오 흐름이 없다(음식점 시드 대기). 시연에서 "안전한 메뉴가 없을 때 다른 식당"(`insight.design_decision`)을 보여 줄지, 보여 주면 시드 몇 곳이 필요한지 정한다(`erd.md` 열린 질문 7) → 흐름은 생김(`scenarios.yaml > other-restaurants-halal`, `e2e/scenarios/other-restaurants-halal.spec.ts`). 시드가 "(예시)" 5곳인 것은 아래 "전체 점검 > 팀 결정 필요 > OPT-2 시드"에 남는다
- (E, 지표) "15초 안에 판정과 확인할 재료까지"를 잴 시작·끝과 조건이 없다. 판독 시간 한도는 45초(REQ-2)라 15초를 넘어도 실패가 아니다. 제안: "사진을 고른 때부터 판정 화면이 보일 때까지, 시드 메뉴판 사진(E1·E2 시나리오)으로 N회 중 N회 15초 이내"처럼 측정 방법을 `measurable`에 적고, 시연 리허설에서 잰 값을 남긴다 → 2026-10-30 반영: `measurable`에 사진 선택 → 배지 표시, E1·E2 사진 10회 중 10회 15초 이내로 적음
- (E, 지표) "포기한 끼니 한 달 약 10끼"는 지금 값만 있고 목표·측정 방법이 없으며 해커톤 기간에는 잴 수 없다. 제안: 목표 숫자를 학생이 정하거나, "배경 지표(해커톤 중 측정 안 함)"로 표시한다 → 2026-10-30 반영: 목표 한 달 5끼 이하, 해커톤 뒤 사용 전후 각 한 달 기록으로 적음
- (E, 지표) 판정이 맞는지 보는 지표가 없다. 서비스의 약속은 "같은 기준으로 판정하고 근거를 보여 준다"인데 성공 지표는 속도뿐이다. 제안: "시드 메뉴 N개에서 기대 판정(시나리오 expected)과 일치 N/N" 같은 정확도 지표를 더할지 정한다(새 지표라 직접 넣지 않음) → 2026-10-30 반영: 배지를 정해 둔 시나리오 5개 5/5 일치, 확인된 제한 재료 메뉴에 "Safe" 0건
- (E, 근거) `root_problem`의 "할랄 메뉴 같은 대응은 비용 부담 때문에 일부 학교에서만 하고 있다(R4)"와 `five_whys` 4번째의 "일부 학교만 감당한다(R4)"는 R4(2018년 서울대 한 곳 도입·적자 운영 기사)보다 넓게 말한다. R4는 다른 학교 현황을 말하지 않는다. 제안: "서울대도 2018년에야 식당 한 곳에 도입했고 적자로 운영한다(R4)"처럼 근거만큼 줄이거나, 여러 학교 현황 출처를 학생이 찾아 더한다 → 2026-10-30 반영: 세 곳을 "서울대도 2018년에야 식당 한 곳에 들였고 적자로 운영"(R4)으로 좁힘
- (F, REQ-4) 할랄 프리셋은 종교를, 알레르기 토글은 건강 정보를 짐작하게 하는 민감한 정보인데, 저장할 때 무엇을 왜 서버에 두는지 알리는 문구가 없다. 제안: 설정 화면에 "Your diet settings are saved on our server with an anonymous number only." 같은 한 줄 고지를 둘지, 서버 대신 브라우저(`localStorage`)에만 둘지 ①이 정한다
- (F, REQ-4) `user_id`가 1부터 늘어나는 번호이고 인증이 없어, 다른 번호로 `GET`·`PATCH /api/v1/users/{user_id}/restrictions`를 부르면 남의 식단 제한을 읽고 바꿀 수 있다. 제안: 해커톤 범위에서는 위험을 문서(`api.md` 열린 질문)에 적어 두고, 시간이 되면 추측하기 어려운 값(UUID 등)으로 바꿀지 ①이 정한다(API 경로·필드 변경이라 직접 고치지 않음)
- (F, REQ-4) 저장한 식단 제한을 지우는 방법(API·버튼)이 없다. 지금은 브라우저 `localStorage`를 지워도 서버 행은 남는다. 제안: 삭제 기능을 더할지, "해커톤이 끝나면 DB를 지운다"는 보관 원칙을 문서에 적을지 정한다(기능 추가라 직접 넣지 않음)
- (F, REQ-1·REQ-2) 사진 보관 원칙이 잠정 방식(메모리 30분)에만 있고, 목표인 `images.data bytea`로 옮기면 보관 기간이 정해져 있지 않다(`erd.md` 열린 질문 1). 사진이 외부 AI로 간다는 것도 화면에 알리지 않는다. 사진에는 사람 얼굴·학생증 같은 것이 같이 찍힐 수 있다. 제안: "판독이 끝나면 지운다" 또는 "N분 뒤 지운다"를 ②가 정해 `api.md > 사진 본문 저장`에 적고, 업로드 화면에 "Photos are sent to an AI service to read the menu and are not kept." 같은 고지를 둘지 정한다
- (F, 윤리) 판정은 참고용인데 "Safe" 배지를 그대로 믿을 위험이 있다(앞의 C 항목 R4·R0과 같은 뿌리). 화면에 "최종 확인은 식당에 한다"는 책임 고지가 없다. 제안: 판정 화면에 짧은 고지를 둘지 정한다(화면 문구 추가라 직접 넣지 않음)

## 전체 점검(랄프 루프)

목표: features(REQ-1~8, OPT-1~3)가 동작하고 문서·코드·테스트가 같은 말을 하는 제출·시연 가능 상태. 심사 결과는 `.git/ralph-loop/last-judge.md`를 본다.

### 회차 기록

- 1회차(2026-10-30) 지난 심사(68점) 지적 "컨트롤러·통합 테스트(409, 413·415, 판정 API)가 없다"를 고침
  - 앞 회차가 커밋하지 못한 컨트롤러 테스트 3개(`ImageControllerTest`·`JudgmentControllerTest`·`RestrictionControllerTest`, 20개)를 검사해 커밋함: 사진 201·400·413·415, 판정 응답 필드·R0·R1·R4·빈 메뉴 400·수동 입력, 식단 제한 PATCH 409 다섯 경우·404·없는 토글 400
  - `RestaurantControllerTest`(5개)를 더함: OPT-2 필터(켠 제한을 모두 피하는 음식점만), `limit` 기본 5·범위 1~20, 맞는 곳 없음 200 `[]`, `user_id` 누락 400, 없는 사용자 404
  - 검사: `./gradlew --no-daemon test` 통과(71개, 실패 0, skip 0). 이 PC에는 JDK 17만 있어 `build.gradle`(Java 21)이 컴파일되지 않는다. 저장소 밖(`~/jdks`)에 Temurin 21을 받아 `JAVA_HOME`으로 돌렸다(저장소 파일은 바꾸지 않음)
  - 건드리지 않은 것: 팀 브랜치 `docs/plan-evidence-fixes`가 `docs/api.md`·`docs/problem.yaml`·`docs/scenarios.yaml`·`docs/decisions.md`·`progress.md`를 고치는 중이라 문서는 고치지 않았다. `progress.md`는 이 절만 맨 아래에 더했다
  - 미확인(node·docker 없음): 흐름 E2E(`--project=scenarios`)와 `docker compose up`은 이 PC에서 돌릴 수 없다. 심사의 "E2E 실행 증거"는 node·docker가 있는 팀원 PC에서 남겨야 한다
  - 다음: `docs/plan-evidence-fixes`가 main에 합쳐진 뒤 심사의 consistency 지적(scenarios.yaml OPT-2 "음식점 시드 대기", progress.md "현재 상태")을 고친다. `MenuController`(REQ-2) HTTP 테스트도 아직 없다
- 2회차(2026-10-30) 지난 심사(65점)의 consistency·tests 지적을 고침. `origin/main`을 충돌 없이 합침(`docs/plan-evidence-fixes`가 main에 들어와 scenarios.yaml OPT-2 머리 주석·흐름 `other-restaurants-halal`은 이미 고쳐짐)
  - `docs/api.md > OPT-2`: `limit` 요청 칸에 "기본 5, 1~20, 범위 밖은 가까운 끝값"을 코드(`RestaurantService.MAX_LIMIT`, `Math.clamp`)대로 적음. "팀이 조사한 음식점 시드"를 사실대로 "지금은 '(예시)' 예시 데이터 5곳"으로 고치고, 추천 순서가 번호 순(기준 미정)임을 적음
  - `e2e/scenarios/other-restaurants-halal.spec.ts`를 더함: scenarios.yaml 단계(Diet settings → Halal → Save → Other restaurants) 그대로, 목록이 1곳 이상이고 모든 카드에 이름·종류와 "Free of … Pork", "Free of … Alcohol"이 보이는지 확인. 시드 기준 기대 결과는 1·4번 2곳
  - `MenuControllerTest`(6개)를 더함: REQ-2 응답 snake_case(`image_id`·`image_type`·`menus[].index·name`), 못 읽은 칸 `name: null`, FOOD는 1개, `image_id` 누락 400, 없는 사진 404 "Image not found.", NOT_MENU 422, AI 오류 500 "We couldn't read the menu. Please type the menu name."
  - 검사: `./gradlew --no-daemon test` 통과(77개, 실패 0, skip 0, JDK 21은 저장소 밖 `~/jdks`). spec은 기존 spec·`RestaurantRecommendations.tsx`·`RestrictionSettings.tsx`(Save 뒤 `/`로 이동)를 다시 읽어 선택자·import를 맞춤
  - 미확인(node·docker 없음): 새 spec 실행. 팀 브랜치 `docs/e2e-results`의 progress.md 3단계에 "로컬 E2E 흐름 7/7 통과 2회"가 적혀 있으나 아직 main에 없고, 새 spec(8번째 흐름)은 그 뒤에 생겨 돌린 적이 없다
  - 건드리지 않은 것: `docs/e2e-results`가 바꾸는 `.github/workflows/scenarios.yml`·`docker-compose.yml`·`e2e/playwright.config.ts`·`frontend/Dockerfile`·`frontend/src/app/%5F%5Fhealth`·README.md. `progress.md`는 이 절만 고침
  - 다음: `docs/e2e-results`가 main에 합쳐지면 8개 흐름을 다시 돌린 결과를 6단계에 옮겨 적을지 확인. 판정 규칙 R0~R4가 Flyway 시드(`R__30_seed_foods`)와 맞는지 보는 DB 통합 테스트는 docker(Testcontainers)가 없어 여기서 못 돌린다
- 3회차(2026-10-30) 지난 심사(64점)의 tests·features 지적을 고침. `origin/main`은 새 커밋이 없어 합칠 것이 없었다(금지 파일 변경 없음)
  - `IngredientInferenceServiceTest`(9개)를 더함: REQ-5 찾는 순서. 음식 DB에 있으면 CONFIRMED·DB이고 외부를 부르지 않음, 띄어쓰기만 다른 이름도 DB에서 찾음, DB에 없으면 만개의 레시피(INFERRED·HIGH, `source=RECIPE_SEARCH`로 저장)이고 AI는 부르지 않음, 레시피도 없으면 AI 추정(MEDIUM, `AI_ESTIMATE`로 저장), 사전에 있는 재료는 사전의 제한 항목, 겹치는 재료는 앞 단계 결과를 남김, 아무것도 없으면 빈 목록·저장 안 함, 저장 충돌에도 판정 진행, 빈 이름 400
  - 화면: `page.tsx`의 지난 TODO("임시 서비스 이름")를 decisions.md 결정으로 바꿈. `useJudgment.ts`가 따로 두던 판정 타입·요청 함수를 ③의 `types/judgment.ts`·`services/judgmentService.ts`(`judge`)로 바꾸고 기존 이름(`JudgmentResult` 등)은 다시 내보냄(두 타입은 필드가 같다)
  - `other-restaurants-halal.spec.ts`: 지금 시드에서 빠져야 하는 2·3·5번(술을 피하지 않음)이 화면에 없는지 확인을 더함
  - 검사: `./gradlew --no-daemon test` 통과(86개, 실패 0, skip 0, JDK 21은 저장소 밖 `~/jdks`)
  - 미확인(node·docker 없음): `useJudgment.ts` 타입 검사·빌드와 바뀐 spec 실행. import 경로(`@/services/judgmentService`, `@/types/judgment`)와 쓰지 않게 된 `apiRequest` import 제거는 코드를 다시 읽어 확인함
  - 다음: 위험 완화(추측하기 어려운 ID·설정 삭제·고지 문구)는 기능 추가라 아래 "팀 결정 필요"에 적었다. 남은 일은 대부분 팀 결정이나 node·docker 확인이다
- 4회차(2026-10-30) 지난 심사(72점)의 위반 두 건을 다룸. `origin/main`은 새 커밋이 없어 합칠 것이 없었다
  - `other-restaurants-halal.spec.ts`: 3회차에 넣은 시드 이름('(예시) …') 확인을 뺐다. scenarios.yaml 머리 주석("실제 조사 데이터로 바뀌어도 그대로 통과")과 spec 3행 주석에 맞춘다. 빠져야 할 음식점이 나오면 그 카드에 "Free of … Alcohol"이 없어 카드별 확인에서 실패하므로 확인 범위는 줄지 않는다(`RestaurantRecommendations.tsx`가 `matched_restrictions`로 문구를 만듦)
  - OPT-2 시드 위반: 실제 음식점 데이터는 지어낼 수 없고, `problem.yaml`은 팀 브랜치 `docs/measurements-in-problem`이 고치는 중이라 건드리지 않았다. 아래 "팀 결정 필요 > OPT-2 시드"에 그대로 남긴다
  - 판정 규칙 점검: 심사가 짚은 "AI 추정에 이름 단어 추정이 섞이면 R4(Safe)" 경우를 코드로 확인했다. 아래 "팀 결정 필요 > R3 범위"에 적었다(규칙 뜻을 바꾸는 일이라 고치지 않음)
  - 검사: 백엔드를 바꾸지 않아 gradle은 돌리지 않았다. spec은 다시 읽어 남은 선택자·import를 확인함
  - 미확인(node·docker 없음): 바뀐 spec 실행
  - 다음: 팀이 R3 범위를 정하면 `JudgmentRules`·`JudgmentRulesTest`·api.md 판정 규칙 표를 같은 커밋에서 고친다
- 5회차(2026-10-30) 지난 심사(66점)의 위반(OPT-2 E2E 제외 확인 약화)과 "가장 먼저 고칠 것"(R3)을 고침. `origin/main`의 화면 문구 표 추가(api.md·decisions.md)를 충돌 없이 합침
  - R3(2e4ab13): 조건을 "음식 DB(`DB`)·만개의 레시피(`RECIPE_SEARCH`) 재료가 하나도 없다"로 고침. REQ-5 done("재료가 모두 AI 추정이면 '확인 필요'")·api.md("AI 추정만으로는 늘 CHECK")의 뜻이 코드에서 깨지던 것을 맞춘 것이라 뜻을 바꾼 것이 아니라고 봄. AI 추정+조리법 추정(두부조림), 이름 단어만 남은 경우(치즈떡볶이)는 R3, 만개의 레시피 재료가 있으면 R4인 테스트 3개를 더함. 캐시된 음식(`foods`)은 처음 basis(`AI_ESTIMATE`·`RECIPE_SEARCH`)를 그대로 가져 결과가 같다
  - OPT-2(6e6b129): `matched_restrictions`를 사용자가 켠 값 대신 음식점이 피하는 항목 전부로 돌려줌. `erd.md`("`restaurant_restriction_items` = 응답의 `matched_restrictions`")와 맞추고 api.md 필드 설명을 고침. 이제 카드의 "Free of …"가 음식점 데이터에서 나오므로, 돼지고기·술을 쓰는 음식점이 섞이면 spec의 카드별 확인이 실패한다(시드 이름에 기대지 않음). 화면 코드는 그대로(`RestaurantRecommendations.tsx`가 이 값을 그대로 보여 줌)
  - api.md 열린 질문 5~7(01257c5): 민감정보 저장·외부 AI 전송 고지 없음, 설정 삭제 수단 없음, R4가 일반 레시피에 기대는 범위
  - 검사: `./gradlew --no-daemon test` 통과(89개, 실패 0, skip 0, JDK 21은 저장소 밖 `~/jdks`)
  - 미확인(node·docker 없음): `other-restaurants-halal` spec 실행. 화면의 R3 설명 "All ingredients are AI estimates"·`reason` 문구는 이름 단어·조리법 추정만 있는 경우에는 넓게 맞는 말이라 그대로 둠(아래 팀 결정 필요)
  - 건드리지 않은 것: "현재 상태"·3단계 체크리스트는 `docs/e2e-results` 브랜치가 고치는 중이라 그대로 둠
  - 다음: 팀 결정 필요 목록(위험 완화·OPT-2 시드·E2E 증거)을 팀이 정한다. 루프가 혼자 할 수 있는 큰 일은 거의 남지 않았다
- 6회차(2026-10-30) 지난 심사(75점)의 정합성 지적을 고침. `origin/main`의 measurements.md → problem.yaml 주석 이동을 충돌 없이 합침
  - R3 문구: 화면 규칙 설명(`labels.ts`, api.md 화면 문구 표)을 "No ingredients from the food database or 10000recipe"로, `JudgmentRules` R3 `reason`을 "Ingredients are only estimated, not from the food database or 10000recipe. Please check before eating."으로 바꿔 5회차의 새 R3 조건과 맞춤. `Rule.R3` 주석도 고침. E2E·테스트는 이 문구를 검사하지 않음(grep 확인)
  - decisions.md: R3 조건 변경(2e4ab13)과 문구를 새 행으로 남기고, 2026-10-29 판정 규칙 행에 바뀌었다는 표시를 붙임. ③ 확인 대기로 적음
  - api.md OPT-2 예시 `matched_restrictions`를 `["alcohol", "pork"]`로, 설명 "이름순"을 "`code` 사전순"으로 고침(`RestaurantService`가 `TreeSet`으로 code를 정렬)
  - 검사: `./gradlew --no-daemon test` 통과(89개, 실패 0, skip 0, JDK 21은 저장소 밖 `~/jdks`)
  - 미확인(node·docker 없음): `labels.ts` 바뀐 화면 표시. 문자열 한 줄만 바뀌어 타입·import 변화 없음
  - 건드리지 않은 것: "현재 상태"·3·6단계는 `docs/e2e-results` 브랜치가 고치는 중이라 그대로 둠. E2E 실행 증거는 이 PC에서 만들 수 없음
  - 다음: 남은 일은 팀 결정(위험 완화 고지·OPT-2 실제 음식점·E2E 결과 병합·R3 ③ 확인·배포 대상)과 node·docker 확인뿐이다
- 7회차(2026-10-30) 지난 심사(73점)의 tests·consistency 지적을 고침. `origin/main`은 새 커밋이 없어 합칠 것이 없었다
  - `JudgmentRulesTest`: R3 두 `reason`(재료 없음, DB·만개의 레시피 재료 없음)과 R0·R4 `reason`을 api.md(판정 규칙 아래 설명, 응답 예시) 문구 그대로 `assertEquals`로 고정함. R3 테스트 4개에 `matched_restrictions`가 빈 목록인지도 확인함
  - 2-B 팀 결정 필요의 옛 문장 "OPT-2는 시나리오 흐름이 없다"에 흐름이 생겼다는 표시를 붙임
  - 검사: `./gradlew --no-daemon test` 통과(89개, 실패 0, skip 0, JDK 21은 저장소 밖 `~/jdks`). 테스트 수는 그대로이고 확인만 늘었다
  - 건드리지 않은 것: "현재 상태"·3~6단계는 `docs/e2e-results`가, api.md·problem.yaml·decisions.md·erd.md는 새 팀 브랜치 `feat/opt2-nearby-restaurants`가 고치는 중이라 그대로 둠
  - 미확인(node·docker 없음): 심사의 "가장 먼저 고칠 것"(docker에서 흐름 E2E 8개 실행)은 이 PC에서 할 수 없다
  - 다음: `docs/e2e-results`·`feat/opt2-nearby-restaurants`가 main에 합쳐지면 "현재 상태"·OPT-2 문서와 코드가 맞는지 다시 본다
- 8회차(2026-10-30) 지난 심사(71점)는 위반이 없고 "가장 먼저 고칠 것"(docker에서 흐름 E2E)은 이 PC에서 못 한다. `origin/main`은 새 커밋이 없었다. OPT-2 시드·문서는 `feat/opt2-nearby-restaurants`가 계약을 바꾸는 중이고, "현재 상태"·3~6단계는 `docs/e2e-results`가 고치는 중이라 건드리지 않았다
  - 실패 처리 빈틈을 고침: `GlobalExceptionHandler`의 마지막 `Exception` 처리가 잘못된 메서드(예: `GET /api/v1/judgments`)와 JSON이 아닌 본문을 받아 500 "Something went wrong on the server."와 오류 로그를 냈다. 405 "This method is not allowed."·415 "This content type is not supported."로 돌려주게 함(공통 에러 형식 `detail` 유지)
  - `JudgmentControllerTest`에 두 경우를 더함. 고치기 전에 두 테스트가 실패하는 것을 먼저 확인함
  - 검사: `./gradlew --no-daemon test` 통과(91개, 실패 0, skip 0, JDK 21은 저장소 밖 `~/jdks`)
  - 문서: `docs/api.md` 공통 표에 405·415 줄은 아직 없다. api.md는 OPT-2 팀 브랜치가 고치는 중이라 그 브랜치가 합쳐진 뒤 더한다
  - 다음: OPT-2 브랜치 병합 뒤 api.md 공통 에러에 405·415를 적고, OPT-2 새 계약과 코드가 맞는지 본다
- 9회차(2026-10-30) 지난 심사(71점)는 api.md 공통 에러에 405·415가 없는 것을 어긋남으로 봤다. `docs/api.md`는 아직 `feat/opt2-nearby-restaurants`가 고치는 중이라(`origin/main`은 새 커밋 없음) 이번에도 건드리지 않고 "팀 결정 필요"에 적었다. 대신 심사가 짚은 "시드와 판정 규칙을 함께 보는 테스트 없음"을 채웠다
  - `SeedJudgmentTest`를 더함: Flyway 시드(`R__10`·`R__30`)의 VALUES 줄을 읽어 `JudgmentRules`에 넣고, `docs/scenarios.yaml` expected와 같은 판정이 나오는지 본다. scan-menu-board(채식: 설렁탕 R1·콩나물국 CHECK·그린샐러드 R4·버섯리조또 R2), diet-toggle-only(Meat → 설렁탕 Avoid), halal-bibimbap-gochujang(R2), peanut-allergy-bibimguksu(R2), manual-input-fallback(할랄 김치찌개 R1), 예시 메뉴 7개가 시드에 있고 된장찌개는 없음, 프리셋 3개가 api.md 열거형과 같음, 시드 재료의 제한 항목이 단어표(`IngredientKeywords`) 결과를 모두 담음
  - PostgreSQL이 없어 SQL을 실제로 실행하지는 않는다. DB 조회 경로는 흐름 E2E가 맡는다
  - 검사: `./gradlew --no-daemon test` 통과(100개, 실패 0, skip 0, JDK 21은 저장소 밖 `~/jdks`). 시드의 비빔밥 `고추장 속 다진 돼지고기`를 잠시 CONFIRMED로 바꾸면 halal-bibimbap 테스트가 실패하는 것을 확인하고 되돌림
  - 미확인(node·docker 없음): 흐름 E2E 8개 실행
  - 다음: OPT-2 브랜치 병합 뒤 api.md 공통 에러에 405·415를 적는다
- 10회차(2026-10-30) 지난 심사(70점)의 "가장 먼저 고칠 것"은 docker에서 흐름 E2E 8개 실행과 api.md 405·415다. `origin/main`은 새 커밋이 없었다. `docs/api.md`는 `feat/opt2-nearby-restaurants`가 아직 고치는 중이라(마지막 커밋 2026-10-30 02:04) 이번에도 건드리지 않았다. 대신 `docs/e2e-results` 브랜치가 적은 "약한 확인" 네 가지를 spec에서 고쳤다
  - `diet-toggle-only`: "고기"를 카드 전체에서 찾던 것을 근거 한 줄(`reason`)에 "소고기"와 "Meat"가 있는지로 바꿈
  - `scan-menu-board`: "소고기"를 카드 전체가 아닌 근거 한 줄에서 찾음
  - `scan-food-photo-single-dish`: 근거 한 줄이 영어 R1 문장(`Contains …소고기…, which is not allowed for your … restriction.`)인지, 메뉴 이름이 원문 "설렁탕" 그대로인지 확인을 더함
  - `manual-input-fallback`: 주석의 옛 한국어 이름("메뉴 이름"·"판정하기")을 화면 이름("Menu name"·"Can I eat it?")으로 고침
  - 기대 문구 근거: `JudgmentRules` R1 reason(재료 이름 최대 2개), 시드 `R__30`의 설렁탕 재료(소고기·소뼈 육수 둘 다 `meat`, 최대 2개 안이라 "소고기"가 늘 들어감), `R__10`의 `meat` 이름 "Meat", `ResultCard.tsx`가 `reason`을 그대로 `data-testid="reason"`에 보임
  - 검사: 백엔드를 바꾸지 않아 gradle은 돌리지 않았다. spec은 다시 읽어 선택자·import를 확인하고 금지 패턴(route·skip·evaluate)이 없는 것을 grep으로 확인함
  - 미확인(node·docker 없음): 바뀐 spec 4개 실행
  - 다음: OPT-2 브랜치 병합 뒤 api.md 공통 에러에 405·415를 적는다. 시드를 실제 PostgreSQL로 확인하는 테스트는 아래 "팀 결정 필요 > 시드 DB 테스트"를 본다
- 11회차(2026-10-30) 지난 심사(67점)의 "가장 먼저 고칠 것"은 docker에서 흐름 E2E 8개 실행과 api.md 405·415다. `origin/main`은 새 커밋이 없었다. `docs/api.md`·`docs/problem.yaml`은 `feat/opt2-nearby-restaurants`가, "현재 상태"·3~6단계는 `docs/e2e-results`가 아직 고치는 중이라(새 커밋 없음) 건드리지 않았다. 대신 테스트가 하나도 없던 REQ-4 첫 단계 API를 채웠다
  - `UserControllerTest`를 더함: `POST /api/v1/users`가 바디 없이 201과 snake_case `user_id`·`created_at`을 돌려주는지(camelCase 이름이 새지 않는지), 부를 때마다 새 번호를 주는지, `GET`이면 405와 공통 에러 `detail`을 주는지 본다. 서비스는 진짜, 저장소만 가짜(Auto Increment 흉내)
  - 검사: `./gradlew --no-daemon test` 통과(103개, 실패 0, skip 0, JDK 21은 저장소 밖 `~/jdks`)
  - 미확인(node·docker 없음): 흐름 E2E 8개 실행
  - 다음: OPT-2 브랜치 병합 뒤 api.md 공통 에러에 405·415를 적는다. 남은 큰 일은 팀 결정과 node·docker 확인이다
- 12회차(2026-10-30) 지난 심사(70점)의 "가장 먼저 고칠 것"은 docker에서 흐름 E2E 8개 실행, api.md 405·415, "현재 상태" 고치기다. `origin/main`은 새 커밋이 없었다. `docs/api.md`는 `feat/opt2-nearby-restaurants`와 새 팀 브랜치 `fix/seed-name-match`가, "현재 상태"·3~6단계는 `docs/e2e-results`가 고치는 중이라 이번에도 건드리지 않았다. `fix/seed-name-match`가 바꾸는 재료 추론 파일(`IngredientInferenceService`·`MenuNameNormalizer`·`RecipeSearchClient`·`FoodRepository`)과 프런트 프록시도 피했다
  - 위험 항목 중 이미 구현된 사진 보관 원칙(메모리 30분, 최대 200장)에 직접 테스트가 없어 `ImageBodyStoreTest`(4개)를 더함: 본문·형식 그대로 돌려줌, 없는 사진은 빈 값, 꼭 30분까지 남고 1초 뒤 지움(시간을 되돌려도 다시 나오지 않음), 201장째에 가장 오래된 사진을 지움
  - 시간을 정하려고 `ImageBodyStore`에 `Clock`을 받는 package-private 생성자를 더함. 동작·보관 기간은 그대로이고 Spring은 인자 없는 생성자를 쓴다(`ImageControllerTest` 통과로 확인)
  - 검사: `./gradlew --no-daemon test` 통과(107개, 실패 0, skip 0, JDK 21은 저장소 밖 `~/jdks`). `TTL`을 31분으로 잠시 바꾸면 만료 테스트가 실패하는 것을 확인하고 되돌림
  - 미확인(node·docker 없음): 흐름 E2E 8개 실행
  - 다음: 남은 일은 팀 결정(아래 목록)과 node·docker 확인뿐이다. 루프가 팀 브랜치를 피해 혼자 할 수 있는 일은 남지 않았다

### 팀 결정 필요

- (시드 DB 테스트) 심사는 `SeedJudgmentTest`가 실제 DB가 아니라 SQL VALUES 줄을 정규식으로 읽는다고 본다. 이 PC에는 docker가 없어 Testcontainers를 못 쓴다. docker 없이 실제 PostgreSQL을 띄우는 `io.zonky.test:embedded-postgres`(testImplementation)를 쓰면 Flyway 시드 → JPA 조회 → 판정까지 테스트할 수 있으나, `build.gradle`은 ①만 고친다(파일 머리 주석). 제안: ①이 이 의존성을 더할지 정한다. 더하면 루프가 통합 테스트를 쓴다

- (api.md 405·415) 8회차가 코드에 넣은 405 "This method is not allowed."·415 "This content type is not supported."가 `docs/api.md` 공통 에러 표에 없다. api.md는 `feat/opt2-nearby-restaurants`·`fix/seed-name-match`가 고치는 중이라 루프가 고치면 충돌한다. 제안: OPT-2 브랜치 담당자가 병합 전에 공통 에러 표에 두 줄을 더하거나, 병합 뒤 루프가 더하게 둔다

- (OPT-2) 코드는 `limit`을 1~20으로 맞추는데(`RestaurantService.MAX_LIMIT`), `docs/api.md > OPT-2`에는 "기본 5"만 있다 → 2회차 반영: api.md 요청 표에 적음
- (OPT-2 시드) 음식점 시드는 "(예시)" 가짜 5곳이고 주소는 `TODO(확인)`이다. `problem.yaml > OPT-2 > done`은 "팀이 조사한 음식점"을 말한다. 실제 음식점 이름·종류·주소·안 쓰는 재료는 AI가 지어낼 수 없다. 제안: ①이 학교 주변 음식점 5곳 이상을 직접 조사해 `R__20_seed_restaurants.sql`을 바꾸거나, 시연에서 OPT-2를 "예시 데이터"라고 밝히고 보여 준다
- (E2E 증거) `docs/e2e-results` 브랜치의 로컬 E2E 결과(흐름 7/7 2회)를 main에 합칠지 정한다. 합친 뒤 새 흐름 `other-restaurants-halal`까지 8개를 한 번 더 돌려 적으면 심사의 "E2E 실행 증거"가 채워진다
- (E2E·CI) 흐름 E2E 7개를 돌린 기록이 없다. CI에 붙이면 실제 AI 호출로 팀 예산($10)이 준다. 제안: node·docker가 있는 PC에서 `docker compose up` 뒤 `npx playwright test --project=scenarios`를 한 번 돌려 결과를 6단계에 적고, CI에는 수동 실행(`workflow_dispatch`)으로만 붙일지 정한다
- (위험 완화) 심사가 "위험은 문서에 있으나 완화 조치가 구현되지 않았다"고 본다: 순서대로 매기는 `user_id`로 남의 식단 설정을 읽고 고칠 수 있음, 설정 삭제 방법 없음, 알레르기·종교 정보와 사진 외부 AI 전송에 대한 고지 문구 없음. 모두 `features`에 없는 기능이거나 API·DB를 바꾸는 일이라 루프가 만들지 않는다. 제안: ①시연 범위에서는 첫 화면에 짧은 영어 고지(사진은 판독에만 쓰고 30분 뒤 지움, 외부 AI로 보냄)를 넣을지 정하고 api.md 화면 문구에 먼저 적는다 ②`user_id`를 UUID로 바꿀지는 새 V 마이그레이션과 api.md 변경이 필요해 제출 뒤로 미룰지 정한다
- (R3 범위, 안전) `api.md > 판정 규칙`의 R3는 "재료가 모두 `AI_ESTIMATE`에서 왔다"이고 `JudgmentRules`도 글자 그대로 `allMatch(AI_ESTIMATE)`다. 그런데 `IngredientInferenceService`는 AI 추정 결과에 이름 단어(`NAME_KEYWORD`)·조리법(`COOKING_METHOD`) 추정을 합친다. 예: DB·만개의 레시피에 없는 "두부조림"을 AI가 [두부, 간장]으로 추정하면 조리법 "조림"이 `맛술`(COOKING_METHOD)을 더해 "모두 AI_ESTIMATE"가 깨지고, 땅콩 알레르기 사용자에게 R4 `SAFE`가 나온다. AI가 실패해 이름 단어 하나(예: "치즈…" → 치즈)만 남아도 같다. 같은 문서 아래 줄("AI 추정만으로는 … 걸리지 않으면 R3로 늘 CHECK")의 뜻과 어긋난다. 제안: R3를 "걸리는 재료가 없고, 음식 DB(`DB`)나 만개의 레시피(`RECIPE_SEARCH`)에서 온 재료가 하나도 없다"로 바꿀지 ③이 정한다. 정하면 `JudgmentRules`·`JudgmentRulesTest`·api.md 표를 함께 고친다 → 5회차에 심사 지적대로 제안안으로 고침(REQ-5 done과 맞춤). ③이 다르게 정하면 2e4ab13을 되돌린다
- (R3 문구) R3가 이름 단어·조리법 추정만 있는 경우도 덮게 되어, 화면 설명 "All ingredients are AI estimates"와 `reason` "Ingredients are AI estimates only."가 조금 좁다. 제안: "No ingredients confirmed by a database or recipe" 같은 문구로 바꿀지 ①·③이 정하고, 바꾸면 api.md 화면 문구 표·`labels.ts`·`JudgmentRules`를 함께 고친다 → 6회차에 심사 지적대로 고침(decisions.md에 남김). ①·③이 다른 문구를 정하면 같은 세 곳을 바꾼다
- (병합과 금지 파일) 심사는 `origin/main` 병합으로 들어온 팀원의 `CLAUDE.md` 변경도 금지 파일 변경으로 감점했다. 루프는 지시대로 main을 합칠 수밖에 없다. 제안: 금지 파일을 고치는 팀 PR은 루프 회차 사이에 합치거나, 심사 기준에 "병합으로 들어온 변경은 제외"를 적을지 정한다
- (실행 조건) `build.gradle`은 Java 21을 요구한다. 팀원 PC에 JDK 17만 있으면 테스트가 컴파일 단계에서 멈춘다. 제안: README 실행 조건에 "JDK 21"을 분명히 적거나 Gradle toolchain 자동 내려받기를 쓸지 정한다
