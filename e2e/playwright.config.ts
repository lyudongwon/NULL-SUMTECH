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
    // 키트·주최 측 실행은 E2E_BASE_URL로 주소를 넘긴다.
    baseURL: process.env.E2E_BASE_URL ?? process.env.BASE_URL ?? "http://localhost:3000",
    trace: "retain-on-failure",
    ...devices["Pixel 7"],
  },
  projects: [
    { name: "smoke", testDir: "./smoke" },
    { name: "scenarios", testDir: "./scenarios" },
  ],
});
