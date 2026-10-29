import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");
// AI 판독·추정은 응답이 늦을 수 있다. 채점기는 팀 config(expect 30초)를 쓰지 않으므로 여기서 직접 기다린다.
const AI_WAIT = { timeout: 60_000 };

async function uploadPhoto(page: Page, file: string) {
  await page.getByRole("link", { name: "Upload photo" }).click();
  await page.locator('input[type="file"]').setInputFiles(path.join(FIXTURES, file));
}

test('땅콩 알레르기 유학생이 비빔국수를 찍으면 양념장 속 땅콩 가능성 때문에 "Check first"와 근거가 나온다', async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("switch", { name: "Peanut", exact: true }).check();
  await page.getByRole("button", { name: "Save" }).click();
  await uploadPhoto(page, "bibimguksu.jpg");

  const card = page.getByRole("listitem").filter({ hasText: "비빔국수" });
  await expect(card.getByText("Check first", { exact: true })).toBeVisible(AI_WAIT);
  await expect(card.getByTestId("reason")).toContainText("양념장");
  await expect(card.getByTestId("reason")).toContainText("땅콩");

  await card.getByRole("button", { name: /비빔국수/ }).click();
  await expect(card).toContainText("양념장");
  const peanutRow = card.getByRole("row").filter({ hasText: "땅콩" }).first();
  await expect(peanutRow).toContainText("Estimated");
  await expect(card.getByRole("columnheader", { name: "Confidence" })).toBeVisible();
  await expect(card).toContainText("Rule");
});
