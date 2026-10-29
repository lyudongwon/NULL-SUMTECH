// docs/scenarios.yaml 흐름 S4 (옛 id manual-input-fallback)
import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");
// AI 판독·추정은 응답이 늦을 수 있다. 채점기는 팀 config(expect 30초)를 쓰지 않으므로 여기서 직접 기다린다.
const AI_WAIT = { timeout: 60_000 };

async function uploadPhoto(page: Page, file: string) {
  await page.getByRole("link", { name: "Upload photo" }).click();
  await page.locator('input[type="file"]').setInputFiles(path.join(FIXTURES, file));
}

test("사진에서 메뉴를 하나도 못 읽으면 이름을 직접 입력해 판정받는다", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("button", { name: "Halal" }).click();
  await page.getByRole("button", { name: "Save" }).click();
  await uploadPhoto(page, "no-menu.jpg");

  // 입력칸 "Menu name"과 버튼 "Can I eat it?"은 ②의 /scan 화면 이름이다(docs/scenarios.yaml).
  const input = page.getByLabel("Menu name");
  await expect(input).toBeVisible(AI_WAIT);
  await input.fill("김치찌개");
  await page.getByRole("button", { name: "Can I eat it?" }).click();

  const card = page.getByRole("listitem").filter({ hasText: "김치찌개" });
  await expect(card.getByText(/^(Safe|Check first|Avoid)$/)).toBeVisible(AI_WAIT);
  await expect(card.getByTestId("reason")).not.toBeEmpty();
});
