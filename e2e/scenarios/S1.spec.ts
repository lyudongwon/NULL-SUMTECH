// docs/scenarios.yaml 흐름 S1 (옛 id scan-menu-board)
import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");

async function uploadPhoto(page: Page, file: string) {
  await page.getByRole("link", { name: "Upload photo" }).click();
  await page.locator('input[type="file"]').setInputFiles(path.join(FIXTURES, file));
}

test("메뉴판 사진을 올리면 메뉴마다 3단계 판정 배지와 근거가 나온다", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("button", { name: "Vegetarian" }).click();
  await page.getByRole("button", { name: "Save" }).click();
  await uploadPhoto(page, "menu-board.jpg");

  const cards = page.getByRole("listitem");
  for (const menu of ["설렁탕", "콩나물국", "그린샐러드", "버섯리조또"]) {
    const card = cards.filter({ hasText: menu });
    await expect(card).toHaveCount(1);
    await expect(card.getByText(/^(Safe|Check first|Avoid)$/)).toBeVisible();
    await expect(card.getByTestId("reason")).not.toBeEmpty();
  }
  const seolleongtang = cards.filter({ hasText: "설렁탕" });
  await expect(seolleongtang.getByText("Avoid", { exact: true })).toBeVisible();
  await expect(seolleongtang).toContainText("소고기");
});
