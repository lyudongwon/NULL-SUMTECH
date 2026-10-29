// docs/scenarios.yaml 흐름 S3 (옛 id diet-toggle-only)
import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");

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
  await expect(card.getByText("Avoid", { exact: true })).toBeVisible();
  // 근거 한 줄에 걸린 재료 "소고기"와 켠 제한 "Meat"가 함께 나온다(R1 reason).
  const reason = card.getByTestId("reason");
  await expect(reason).toContainText("소고기");
  await expect(reason).toContainText("Meat");
});
