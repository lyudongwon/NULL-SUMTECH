import { defineConfig } from "@playwright/test";

// 앱은 따로 띄운다(docker compose up -d --build). platform: mobile이라 휴대폰 화면 한 가지로만 돌린다.
// 채점기는 이 파일을 쓰지 않고 시작 키트 설정과 같은 값(390x844 터치 화면, 한 번에 하나씩, expect 10초)으로 돈다.
// 로컬·CI 결과가 채점기와 같도록 그 값에 맞췄다. AI를 기다리는 단언은 spec 안에서 timeout을 직접 준다.
// - smoke: 앱이 살아 있는지 빠르게 본다. 지금 CI에서 돈다.
// - scenarios: docs/scenarios.yaml 흐름. 채점기처럼 S<n>.spec.ts만 실행한다.
// - extra: 흐름(3~8개)에 넣지 않은 부가 기능 확인(OPT-4·OPT-5). 채점기는 돌리지 않고 로컬·CI에서만 돈다.
const mobile = { viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true };

export default defineConfig({
  timeout: 120_000,
  expect: { timeout: 10_000 },
  // 흐름은 S1부터 순서대로 같은 DB에서 돈다(채점기와 같음).
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: 0,
  reporter: [["list"], ["html", { outputFolder: "playwright-report", open: "never" }]],
  outputDir: "test-results",
  use: {
    baseURL: process.env.E2E_BASE_URL ?? process.env.BASE_URL ?? "http://localhost:3000",
    locale: "ko-KR",
    timezoneId: "Asia/Seoul",
    serviceWorkers: "block",
    trace: "retain-on-failure",
    ...mobile,
  },
  projects: [
    { name: "smoke", testDir: "./smoke" },
    // 채점기는 흐름 id와 같은 이름의 spec만 돌린다(e2e/scenarios/S1.spec.ts…). 로컬·CI도 같은 조건으로 맞춘다.
    { name: "scenarios", testDir: "./scenarios", testMatch: /[\\/]S[1-9]\d*\.spec\.ts$/ },
    // scenarios 뒤에 돈다. 제보 확인은 끝에서 제보를 되돌려 S8의 목록을 줄이지 않는다.
    { name: "extra", testDir: "./extra" },
  ],
});
