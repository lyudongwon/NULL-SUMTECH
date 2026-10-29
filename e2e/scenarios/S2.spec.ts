// docs/scenarios.yaml 흐름 S2 (옛 id scan-food-photo-single-dish)
import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");
// AI 판독·추정은 응답이 늦을 수 있다. 채점기는 팀 config(expect 30초)를 쓰지 않으므로 여기서 직접 기다린다.
const AI_WAIT = { timeout: 60_000 };

async function uploadPhoto(page: Page, file: string) {
  await page.getByRole("link", { name: "Upload photo" }).click();
  await page.locator('input[type="file"]').setInputFiles(path.join(FIXTURES, file));
}

test("음식 사진 한 장을 올리면 요리명 1개와 판정, 근거 상세가 나온다", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("button", { name: "Vegetarian" }).click();
  await page.getByRole("button", { name: "Save" }).click();
  await uploadPhoto(page, "seolleongtang.jpg");

  const cards = page.getByRole("listitem").filter({ has: page.getByRole("button") });
  await expect(cards).toHaveCount(1, AI_WAIT);
  const card = cards.filter({ hasText: "설렁탕" });
  await expect(card.getByText("Avoid", { exact: true })).toBeVisible();
  await expect(card.getByTestId("reason")).toContainText(/^Contains .+, which is not allowed for your .+ restriction\.$/);

  await card.getByRole("button", { name: /설렁탕/ }).click();
  const beefRow = card.getByRole("row").filter({ hasText: "소고기" }).first();
  await expect(beefRow).toContainText("Confirmed");
  await expect(card.getByRole("columnheader", { name: "Confidence" })).toBeVisible();
  await expect(card).toContainText("Rule");

  await expect(page.getByText("For guidance only. Please make the final check yourself or with a dietitian.")).toBeVisible();
});
