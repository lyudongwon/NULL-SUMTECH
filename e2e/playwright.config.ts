import { defineConfig, devices } from "@playwright/test";

// 앱은 따로 띄운다(docker compose up -d --build). platform: mobile이라 휴대폰 화면 한 가지로만 돌린다.
// - smoke: 앱이 살아 있는지 빠르게 본다. 지금 CI에서 돈다.
// - scenarios: docs/scenarios.yaml 흐름. ②·③ 기능이 들어오면 CI에 붙인다.
export default defineConfig({
  timeout: 60_000, // 흐름 하나가 1분 안에 끝나야 한다(progress.md 6단계). AI 판독·추정을 실제로 기다린다.
  expect: { timeout: 30_000 },
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: 0,
  reporter: [["list"], ["html", { outputFolder: "playwright-report", open: "never" }]],
  outputDir: "test-results",
  use: {
    baseURL: process.env.BASE_URL ?? "http://localhost:3000",
    trace: "retain-on-failure",
    ...devices["Pixel 7"],
    // 채점기와 같은 휴대폰 화면으로 맞춘다: 390x844, isMobile, hasTouch(Pixel 7의 기본은 412x915다).
    viewport: { width: 390, height: 844 },
  },
  projects: [
    { name: "smoke", testDir: "./smoke" },
    // 채점기는 흐름 id와 같은 이름의 spec만 돌린다(e2e/scenarios/S1.spec.ts…). 로컬·CI도 같은 조건으로 맞춘다.
    { name: "scenarios", testDir: "./scenarios", testMatch: /[\\/]S[1-9]\d*\.spec\.ts$/ },
  ],
});
