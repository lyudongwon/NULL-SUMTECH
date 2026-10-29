// docs/scenarios.yaml 흐름 S5 (옛 id manual-input-unknown-menu)
import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");

async function uploadPhoto(page: Page, file: string) {
  await page.getByRole("link", { name: "Upload photo" }).click();
  await page.locator('input[type="file"]').setInputFiles(path.join(FIXTURES, file));
}

test("음식 DB에 없는 메뉴를 입력하면 만개의 레시피에서 재료를 찾아와 판정한다", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("button", { name: "Halal" }).click();
  await page.getByRole("button", { name: "Save" }).click();
  await uploadPhoto(page, "no-menu.jpg");

  // "된장찌개"는 음식 DB 시드에 넣지 않는 메뉴다(docs/roles.md E2E 예외).
  await page.getByLabel("Menu name").fill("된장찌개");
  await page.getByRole("button", { name: "Can I eat it?" }).click();

  await expect(page).toHaveURL(/\/result/);
  await expect(page.getByRole("alertdialog")).toHaveCount(0);
  const card = page.getByRole("listitem").filter({ hasText: "된장찌개" });
  await expect(card.getByText(/^(Safe|Check first|Avoid)$/)).toBeVisible();
  await expect(card.getByTestId("reason")).not.toBeEmpty();
});
