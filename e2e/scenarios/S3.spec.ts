// docs/scenarios.yaml 흐름 S3 (옛 id diet-toggle-only)
import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");
// AI 판독·추정은 응답이 늦을 수 있다. 채점기는 팀 config(expect 30초)를 쓰지 않으므로 여기서 직접 기다린다.
const AI_WAIT = { timeout: 60_000 };

async function uploadPhoto(page: Page, file: string) {
  await page.getByRole("link", { name: "Upload photo" }).click();
  await page.locator('input[type="file"]').setInputFiles(path.join(FIXTURES, file));
}

test("프리셋 없이 토글만으로 식단 제한을 정해 판정받는다", async ({ page }) => {
  await page.goto("/");
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
  await expect(card).toContainText("고기");
});
