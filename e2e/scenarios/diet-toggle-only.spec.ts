import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");

async function uploadPhoto(page: Page, file: string) {
  await page.getByRole("link", { name: "사진 올리기" }).click();
  await page.locator('input[type="file"]').setInputFiles(path.join(FIXTURES, file));
}

test("프리셋 없이 토글만으로 식단 제한을 정해 판정받는다", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "식단 제한 설정" }).click();
  await page.getByRole("switch", { name: "고기", exact: true }).check();
  await page.getByRole("button", { name: "저장" }).click();

  // 저장한 설정이 남아 있는지 설정 화면에서 다시 본다.
  await page.getByRole("link", { name: "식단 제한 설정" }).click();
  await expect(page.getByRole("switch", { name: "고기", exact: true })).toBeChecked();
  await page.getByRole("link", { name: "처음으로" }).click();

  await uploadPhoto(page, "seolleongtang.jpg");

  const card = page.getByRole("listitem").filter({ hasText: "설렁탕" });
  await expect(card.getByText("불가", { exact: true })).toBeVisible();
  await expect(card).toContainText("고기");
});
