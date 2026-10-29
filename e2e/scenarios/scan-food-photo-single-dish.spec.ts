import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");

async function uploadPhoto(page: Page, file: string) {
  await page.getByRole("link", { name: "사진 올리기" }).click();
  await page.locator('input[type="file"]').setInputFiles(path.join(FIXTURES, file));
}

test("음식 사진 한 장을 올리면 요리명 1개와 판정, 근거 상세가 나온다", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "식단 제한 설정" }).click();
  await page.getByRole("button", { name: "채식" }).click();
  await page.getByRole("button", { name: "저장" }).click();
  await uploadPhoto(page, "seolleongtang.jpg");

  const cards = page.getByRole("listitem").filter({ has: page.getByRole("button") });
  await expect(cards).toHaveCount(1);
  const card = cards.filter({ hasText: "설렁탕" });
  await expect(card.getByText("불가", { exact: true })).toBeVisible();

  await card.getByRole("button", { name: /설렁탕/ }).click();
  const beefRow = card.getByRole("row").filter({ hasText: "소고기" }).first();
  await expect(beefRow).toContainText("확인");
  await expect(card.getByRole("columnheader", { name: "확신도" })).toBeVisible();
  await expect(card).toContainText("적용된 규칙");

  await expect(page.getByText("판정 보조이며, 최종 확인은 본인과 영양사에게.")).toBeVisible();
});
