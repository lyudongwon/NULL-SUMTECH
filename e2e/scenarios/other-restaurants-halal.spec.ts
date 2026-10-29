import { expect, test } from "@playwright/test";

// 음식점 시드(R__20)가 아직 예시 데이터라, 음식점 이름이 아니라 "켠 제한 항목을 모두 피한다"로 확인한다(docs/scenarios.yaml).
test('할랄 유학생이 "Other restaurants"를 누르면 돼지고기·술을 쓰지 않는 음식점만 추천된다', async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("button", { name: "Halal" }).click();
  await page.getByRole("button", { name: "Save" }).click();
  await page.getByRole("link", { name: "Other restaurants" }).click();

  const restaurants = page.getByRole("listitem");
  await expect(restaurants.first()).toBeVisible();
  await expect(page.getByText("No restaurants match your diet yet.")).toHaveCount(0);

  // 추천된 음식점은 모두 Halal 제한 항목(Pork·Alcohol)을 피한다고 표시된다.
  for (const restaurant of await restaurants.all()) {
    await expect(restaurant).toContainText("Free of");
    await expect(restaurant).toContainText("Pork");
    await expect(restaurant).toContainText("Alcohol");
  }
});
