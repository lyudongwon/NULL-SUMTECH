import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");

async function uploadPhoto(page: Page, file: string) {
  await page.getByRole("link", { name: "사진 올리기" }).click();
  await page.locator('input[type="file"]').setInputFiles(path.join(FIXTURES, file));
}

test("사진에서 메뉴를 하나도 못 읽으면 이름을 직접 입력해 판정받는다", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "식단 제한 설정" }).click();
  await page.getByRole("button", { name: "할랄" }).click();
  await page.getByRole("button", { name: "저장" }).click();
  await uploadPhoto(page, "no-menu.jpg");

  // 입력칸 이름 "메뉴 이름"과 버튼 "판정하기"는 ②의 /scan 화면 이름이다(docs/scenarios.yaml).
  const input = page.getByLabel("메뉴 이름");
  await expect(input).toBeVisible();
  await input.fill("김치찌개");
  await page.getByRole("button", { name: "판정하기" }).click();

  const card = page.getByRole("listitem").filter({ hasText: "김치찌개" });
  await expect(card.getByText(/^(안전|확인 필요|불가)$/)).toBeVisible();
  await expect(card.getByTestId("reason")).not.toBeEmpty();
});
