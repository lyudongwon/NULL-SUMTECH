// docs/scenarios.yaml 흐름 S1 (옛 id scan-menu-board)
import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");
// AI 판독·추정은 응답이 늦을 수 있다. 채점기는 팀 config(expect 30초)를 쓰지 않으므로 여기서 직접 기다린다.
const AI_WAIT = { timeout: 60_000 };

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
    await expect(card).toHaveCount(1, AI_WAIT);
    await expect(card.getByText(/^(Safe|Check first|Avoid)$/)).toBeVisible();
    await expect(card.getByTestId("reason")).not.toBeEmpty();
  }
  const seolleongtang = cards.filter({ hasText: "설렁탕" });
  await expect(seolleongtang.getByText("Avoid", { exact: true })).toBeVisible();
  // 근거 한 줄은 영어 문장이고 재료 이름만 한국어 원문이다(docs/api.md > 화면 문구).
  await expect(seolleongtang.getByTestId("reason")).toContainText("소고기");
  await expect(seolleongtang.getByTestId("reason")).toContainText(/^Contains .+, which is not allowed for your .+ restriction\.$/);
});
