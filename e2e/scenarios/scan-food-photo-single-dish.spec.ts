import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");

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
  await expect(cards).toHaveCount(1);
  const card = cards.filter({ hasText: "설렁탕" });
  await expect(card.getByText("Avoid", { exact: true })).toBeVisible();
  // 영어 근거 한 줄(R1 reason). 재료 이름만 메뉴판 원문(한국어)으로 들어간다.
  await expect(card.getByTestId("reason")).toHaveText(/^Contains .*소고기.*, which is not allowed for your .+ restriction\.$/);
  await expect(card.getByText("설렁탕", { exact: true })).toBeVisible();

  await card.getByRole("button", { name: /설렁탕/ }).click();
  const beefRow = card.getByRole("row").filter({ hasText: "소고기" }).first();
  await expect(beefRow).toContainText("Confirmed");
  await expect(card.getByRole("columnheader", { name: "Confidence" })).toBeVisible();
  await expect(card).toContainText("Rule");

  await expect(page.getByText("For guidance only. Please make the final check yourself or with a dietitian.")).toBeVisible();
});
