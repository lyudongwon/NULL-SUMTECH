import { expect, test } from "@playwright/test";

test('할랄 유학생이 "Other restaurants"를 누르면 돼지고기·술을 쓰지 않는 음식점만 추천된다', async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("button", { name: "Halal" }).click();
  await page.getByRole("button", { name: "Save" }).click();
  await page.getByRole("link", { name: "Other restaurants" }).click();

  // 음식점 이름이 아니라 "제한 항목을 쓰는 곳은 안 보인다"로 확인한다(docs/scenarios.yaml 주석). 시드가 바뀌어도 통과해야 한다.
  await expect(page.getByRole("heading", { name: "Other restaurants" })).toBeVisible();
  const restaurants = page.getByRole("listitem");
  await expect(restaurants.first()).toBeVisible();
  const count = await restaurants.count();
  expect(count).toBeGreaterThan(0);
  for (let i = 0; i < count; i++) {
    const restaurant = restaurants.nth(i);
    // 음식점마다 이름(첫 줄)과 종류(둘째 줄)가 비어 있지 않다.
    await expect(restaurant.locator("p").nth(0)).not.toBeEmpty();
    await expect(restaurant.locator("p").nth(1)).not.toBeEmpty();
    // 목록에 있는 곳은 Halal의 두 항목을 모두 쓰지 않는다.
    await expect(restaurant).toContainText(/Free of .*Pork/);
    await expect(restaurant).toContainText(/Free of .*Alcohol/);
  }
});
