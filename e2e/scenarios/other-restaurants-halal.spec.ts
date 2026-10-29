import { expect, test } from "@playwright/test";

// 음식점 시드(R__20)가 아직 예시 데이터라, 이름이 아니라 "제한 항목을 쓰는 음식점은 안 보인다"로 확인한다.
test('할랄 유학생이 "Other restaurants"를 누르면 돼지고기·술을 쓰지 않는 음식점만 추천된다', async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("button", { name: "Halal" }).click();
  await page.getByRole("button", { name: "Save" }).click();
  await page.getByRole("link", { name: "Other restaurants" }).click();

  await expect(page.getByRole("heading", { name: "Other restaurants" })).toBeVisible();
  const cards = page.getByRole("listitem");
  await expect(cards.first()).toBeVisible();

  // 음식점마다 이름과 종류가 보이고, 돼지고기·술을 모두 피한다고 적혀 있어야 한다.
  const count = await cards.count();
  for (let i = 0; i < count; i++) {
    const card = cards.nth(i);
    await expect(card.locator("p").nth(0)).not.toBeEmpty();
    await expect(card.locator("p").nth(1)).not.toBeEmpty();
    await expect(card).toContainText(/Free of .*Pork/);
    await expect(card).toContainText(/Free of .*Alcohol/);
  }
});
