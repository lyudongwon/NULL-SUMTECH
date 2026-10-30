// docs/scenarios.yaml 흐름 S5 (옛 id manual-input-unknown-menu)
import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");
// AI 판독·추정은 응답이 늦을 수 있다. 채점기는 팀 config(expect 30초)를 쓰지 않으므로 여기서 직접 기다린다.
const AI_WAIT = { timeout: 60_000 };

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
  await expect(page.getByLabel("Menu name")).toBeVisible(AI_WAIT);
  await page.getByLabel("Menu name").fill("된장찌개");
  await page.getByRole("button", { name: "Can I eat it?" }).click();

  await expect(page).toHaveURL(/\/result/, AI_WAIT);
  await expect(page.getByRole("alertdialog")).toHaveCount(0);
  // 만개의 레시피·AI 추정에서 찾은 재료는 모두 추정(INFERRED)이라 R1 "Avoid"는 나오지 않는다(docs/api.md > REQ-3 판정 규칙).
  // 무엇을 찾아오는지는 외부 사이트·AI에 따라 달라서 배지 하나로 못 박지 않고, 밖에서 찾아왔다는 근거를 본다.
  const card = page.getByRole("listitem").filter({ hasText: "된장찌개" });
  await expect(card.getByText(/^(Safe|Check first)$/)).toBeVisible(AI_WAIT);
  await expect(card.getByTestId("reason")).toHaveText(
    /^(May contain .+\. Check for .+ before eating\.|No ingredients conflict with your diet restrictions\.|Ingredients are AI estimates only\. Please check before eating\.)$/,
  );

  await card.getByRole("button", { name: /된장찌개/ }).click();
  const rows = card.getByRole("row").filter({ has: page.getByRole("cell") });
  await expect(rows.first()).toBeVisible();
  await expect(rows.first()).toContainText("Estimated");
  await expect(rows.first()).toContainText(/10000recipe|AI estimate/);
  await expect(card).toContainText("Rule");
});
