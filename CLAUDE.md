# NULL-SUMTECH

SUMTECH 2026 루프톤 NULL 팀 서비스 저장소. 학식당 메뉴판·음식 사진 한 장으로 내 식단 제한(알레르기·할랄·채식)에 맞는지 **안전 / 확인 필요 / 불가** 3단계로 판정하는 웹 서비스.

이 파일은 문서 지도다. 내용을 여기에 복사하지 말고, 작업에 맞는 문서를 열어 읽는다.

## 문서 지도

| 이럴 때 읽는다 | 문서 | 담고 있는 것 | 상태 |
| --- | --- | --- | --- |
| 작업을 시작할 때 (항상) | [docs/ai-rules.md](docs/ai-rules.md) | AI 에이전트 작업 규칙, 저장소 약속 | 작성됨(초안) |
| 기능을 만들거나 고칠 때, 요구사항을 확인할 때 | [docs/problem.yaml](docs/problem.yaml) | 문제정의 캔버스, 경험 사례, 근거 자료, 기능 명세 `features` (REQ-1~7, OPT-1~3) | 작성됨 |
| 화면·E2E 흐름을 만들거나 고칠 때 | [docs/scenarios.yaml](docs/scenarios.yaml) | 사용자 핵심 흐름(단계·기대 결과), 주 사용 플랫폼, 로그인 여부 | 작성됨 |
| API를 만들거나 호출할 때 | [docs/api.md](docs/api.md) | 엔드포인트별 요청·응답·에러·예외 상황, 열린 질문 | 작성됨(노션 사본) |
| 커밋·브랜치·PR을 만들 때 | [docs/git-convention.md](docs/git-convention.md) | 브랜치, 커밋 메시지, PR, main 복구, 금지 사항 | 작성됨(초안) |
| 채팅·회의에서 정한 결정을 남기거나 확인할 때 | [docs/decisions.md](docs/decisions.md) | 날짜별 결정과 문서 반영 상태 | 작성됨 |
| DB·엔티티·마이그레이션을 다룰 때 | [docs/erd.md](docs/erd.md) | 테이블·관계, 시드 최소 요구, 열린 질문 | 작성됨(초안) |
| 기술 스택과 실행 조건을 확인할 때 | [docs/tech-stack.md](docs/tech-stack.md) | 언어·프레임워크·DB·AI·테스트·배포 선택, 실행 조건 | 작성됨(노션 사본) |
| 세션을 시작할 때 | [progress.md](progress.md) | 현재 단계, 해야 할 일 체크리스트 | 작성됨 |
| 실행·환경변수를 확인할 때 | [README.md](README.md) | 설치·실행 방법, 환경변수 표 | 작성됨(실행 방법은 기술 구성 확정 후) |
| 팀 원본 문서를 볼 때 | [노션: SUMTECH 2026 NULL팀](https://app.notion.com/p/408c4b3d7d9e80c1baf7e49e8365e3e5) | 기획서, 기능 명세서, API 명세서 | 팀 공유 |

- 상태가 "작성 예정"인 문서는 파일이 생긴 뒤에 따른다. 없는 문서의 내용을 지어서 인용하지 않는다.
- `docs/`에 협업 문서를 새로 추가하면 이 표에 한 줄을 같이 추가한다.
- `problem.yaml`의 `features`는 노션 기능 명세서와 같은 ID(REQ·OPT)를 쓴다. 둘이 어긋나면 레포 기준으로 진행하고, 어긋난 부분은 보고해서 팀이 맞추게 한다.

## 작업 규칙

작업을 시작하기 전에 [docs/ai-rules.md](docs/ai-rules.md)를 읽고 따른다. 기본 방침은 사전 승인보다 사후 보고, 사전 검토보다 빠른 테스트와 수정이다. 규칙은 그 문서에만 두고 여기에 옮겨 적지 않는다.

## Git

커밋·브랜치·PR을 만들기 전에 [docs/git-convention.md](docs/git-convention.md)를 먼저 읽고 그대로 따른다. 규칙은 그 문서에만 두고 여기에 옮겨 적지 않는다.

## 기술 스택

TypeScript + Next.js(App Router) + PostgreSQL(Drizzle) + Vercel AI SDK. 전체 표와 실행 조건은 [docs/tech-stack.md](docs/tech-stack.md), 설치·실행 방법은 README에 적는다.
