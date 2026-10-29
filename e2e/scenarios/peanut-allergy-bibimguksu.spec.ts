import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");

async function uploadPhoto(page: Page, file: string) {
  await page.getByRole("link", { name: "사진 올리기" }).click();
  await page.locator('input[type="file"]').setInputFiles(path.join(FIXTURES, file));
}

test('땅콩 알레르기 유학생이 비빔국수를 찍으면 양념장 속 땅콩 가능성 때문에 "확인 필요"와 근거가 나온다', async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "식단 제한 설정" }).click();
  await page.getByRole("switch", { name: "땅콩", exact: true }).check();
  await page.getByRole("button", { name: "저장" }).click();
  await uploadPhoto(page, "bibimguksu.jpg");

  const card = page.getByRole("listitem").filter({ hasText: "비빔국수" });
  await expect(card.getByText("확인 필요", { exact: true })).toBeVisible();
  await expect(card.getByTestId("reason")).toContainText("양념장");
  await expect(card.getByTestId("reason")).toContainText("땅콩");

  await card.getByRole("button", { name: /비빔국수/ }).click();
  await expect(card).toContainText("양념장");
  const peanutRow = card.getByRole("row").filter({ hasText: "땅콩" }).first();
  await expect(peanutRow).toContainText("추정");
  await expect(card.getByRole("columnheader", { name: "확신도" })).toBeVisible();
  await expect(card).toContainText("적용된 규칙");
});
