import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");

async function uploadPhoto(page: Page, file: string) {
  await page.getByRole("link", { name: "사진 올리기" }).click();
  await page.locator('input[type="file"]').setInputFiles(path.join(FIXTURES, file));
}

test('할랄 유학생이 비빔밥을 찍으면 고추장 속 돼지고기 가능성 때문에 "확인 필요"와 근거가 나온다', async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "식단 제한 설정" }).click();
  await page.getByRole("button", { name: "할랄" }).click();
  await page.getByRole("button", { name: "저장" }).click();
  await uploadPhoto(page, "bibimbap.jpg");

  const card = page.getByRole("listitem").filter({ hasText: "비빔밥" });
  await expect(card.getByText("확인 필요", { exact: true })).toBeVisible();
  await expect(card.getByTestId("reason")).toContainText("고추장");
  await expect(card.getByTestId("reason")).toContainText("돼지고기");

  await card.getByRole("button", { name: /비빔밥/ }).click();
  await expect(card).toContainText("고추장");
  const porkRow = card.getByRole("row").filter({ hasText: "돼지고기" }).first();
  await expect(porkRow).toContainText("추정");
  await expect(card.getByRole("columnheader", { name: "확신도" })).toBeVisible();
  await expect(card).toContainText("적용된 규칙");
});
