// docs/scenarios.yaml 흐름 S3 (옛 id diet-toggle-only). account: 유학생 — 시드 계정으로 먼저 로그인한 뒤 steps를 시작한다(로그인은 steps에 쓰지 않는다).
import path from "node:path";
import { expect, test, type Page } from "@playwright/test";
import { E2E_USER_EMAIL, E2E_USER_PASSWORD } from "../env";

const FIXTURES = path.join(__dirname, "..", "fixtures");
// AI 판독·추정은 응답이 늦을 수 있다. 채점기는 팀 config(expect 30초)를 쓰지 않으므로 여기서 직접 기다린다.
const AI_WAIT = { timeout: 60_000 };

async function uploadPhoto(page: Page, file: string) {
  await page.getByRole("link", { name: "Upload photo" }).click();
  await page.locator('input[type="file"]').setInputFiles(path.join(FIXTURES, file));
}

// 로그인은 steps에 쓰지 않지만 화면으로 한다. 첫 화면에서 "Log in"을 눌러 들어간다(시드 계정은 docker-compose.yml).
async function logIn(page: Page, email: string, password: string) {
  await page.goto("/");
  await page.getByRole("link", { name: "Log in" }).click();
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill(password);
  await page.getByRole("button", { name: "Log in" }).click();
  await expect(page.getByRole("button", { name: "Log out" })).toBeVisible();
}

test("로그인한 유학생이 토글만으로 식단 제한을 정해 판정받는다", async ({ page }) => {
  await logIn(page, E2E_USER_EMAIL, E2E_USER_PASSWORD);
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("switch", { name: "Meat", exact: true }).check();
  await page.getByRole("button", { name: "Save" }).click();

  // 저장한 설정이 남아 있는지 설정 화면에서 다시 본다.
  await page.getByRole("link", { name: "Diet settings" }).click();
  await expect(page.getByRole("switch", { name: "Meat", exact: true })).toBeChecked();
  await page.getByRole("link", { name: "Home" }).click();

  await uploadPhoto(page, "seolleongtang.jpg");

  const card = page.getByRole("listitem").filter({ hasText: "설렁탕" });
  await expect(card.getByText("Avoid", { exact: true })).toBeVisible(AI_WAIT);
  await expect(card.getByTestId("reason")).toContainText("소고기");
  await expect(card.getByTestId("reason")).toContainText(/, which is not allowed for your .*Meat.* restriction\.$/);
});
