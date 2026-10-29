// docs/scenarios.yaml 흐름 S7 (옛 id other-restaurants-halal)
// 음식점 시드(R__20)가 아직 예시 데이터라 이름이 아니라 제한 규칙으로 확인한다(docs/scenarios.yaml 머리 주석).
// Halal 프리셋은 Pork·Alcohol을 켜고, 추천 화면은 음식점마다 쓰지 않는 항목을 "Free of ..."로 보여 준다(docs/api.md > 화면 문구).
import { expect, test } from "@playwright/test";

test('할랄 유학생이 "Other restaurants"를 누르면 돼지고기·술을 쓰지 않는 음식점만 추천된다', async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("button", { name: "Halal" }).click();
  await page.getByRole("button", { name: "Save" }).click();
  await page.getByRole("link", { name: "Other restaurants" }).click();

  const cards = page.getByRole("listitem");
  await expect(cards.first()).toBeVisible();

  const count = await cards.count();
  expect(count).toBeGreaterThan(0);
  for (let i = 0; i < count; i += 1) {
    const card = cards.nth(i);
    const lines = card.getByRole("paragraph");
    await expect(lines.nth(0)).not.toBeEmpty(); // 음식점 이름
    await expect(lines.nth(1)).not.toBeEmpty(); // 종류(주소가 있으면 함께)
    await expect(card).toContainText(/Free of .*Pork/); // 돼지고기를 쓰지 않는다
    await expect(card).toContainText(/Free of .*Alcohol/); // 술을 쓰지 않는다
  }
});
