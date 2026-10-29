// docs/scenarios.yaml 흐름 S5 (옛 id halal-bibimbap-gochujang)
import path from "node:path";
import { expect, test, type Page } from "@playwright/test";

const FIXTURES = path.join(__dirname, "..", "fixtures");
// AI 판독·추정은 응답이 늦을 수 있다. 채점기는 팀 config(expect 30초)를 쓰지 않으므로 여기서 직접 기다린다.
const AI_WAIT = { timeout: 60_000 };

async function uploadPhoto(page: Page, file: string) {
  await page.getByRole("link", { name: "Upload photo" }).click();
  await page.locator('input[type="file"]').setInputFiles(path.join(FIXTURES, file));
}

test('할랄 유학생이 비빔밥을 찍으면 고추장 속 돼지고기 가능성 때문에 "Check first"와 근거가 나온다', async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("button", { name: "Halal" }).click();
  await page.getByRole("button", { name: "Save" }).click();
  await uploadPhoto(page, "bibimbap.jpg");

  const card = page.getByRole("listitem").filter({ hasText: "비빔밥" });
  await expect(card.getByText("Check first", { exact: true })).toBeVisible(AI_WAIT);
  await expect(card.getByTestId("reason")).toContainText("고추장");
  await expect(card.getByTestId("reason")).toContainText("돼지고기");
  // 근거 한 줄은 영어 문장이고 재료 이름만 한국어 원문이다(docs/api.md > 화면 문구).
  await expect(card.getByTestId("reason")).toContainText(/^May contain .+\. Check for .+ before eating\.$/);

  await card.getByRole("button", { name: /비빔밥/ }).click();
  await expect(card).toContainText("고추장");
  const porkRow = card.getByRole("row").filter({ hasText: "돼지고기" }).first();
  await expect(porkRow).toContainText("Estimated");
  await expect(card.getByRole("columnheader", { name: "Confidence" })).toBeVisible();
  await expect(card).toContainText("Rule");
});
