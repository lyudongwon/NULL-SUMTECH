// docs/scenarios.yaml 흐름 S10 (음식점 성분 제보와 추천 필터링, OPT-5 + OPT-2 + OPT-4)
// 제보는 DB에 남아 다른 흐름(S8)의 추천 목록을 줄이므로, 끝에서 내 제보를 취소해 원래대로 돌려 놓는다.
import { expect, test } from "@playwright/test";

const EMAIL = `s10-${Date.now()}@example.com`;
const PASSWORD = "s10-test-password";

test("가게에 돼지고기가 쓰인다고 제보하면 할랄 사용자의 추천에서 그 가게가 빠진다", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("button", { name: "Halal" }).click();
  await page.getByRole("button", { name: "Save" }).click();
  await page.getByRole("link", { name: "Sign up" }).click();
  await page.getByLabel("Email").fill(EMAIL);
  await page.getByLabel("Password").fill(PASSWORD);
  await page.getByRole("button", { name: "Sign up" }).click();
  await expect(page.getByRole("button", { name: "Log out" })).toBeVisible();

  await page.getByRole("link", { name: "Other restaurants" }).click();
  const cards = page.getByRole("listitem");
  await expect(cards.first()).toBeVisible();
  const before = await cards.count();
  const name = (await cards.first().getByRole("paragraph").first().textContent()) ?? "";
  expect(name).not.toBe("");

  await cards.first().getByRole("button", { name: "Report an ingredient" }).click();
  await cards.first().getByLabel("Which ingredient is used?").selectOption({ label: "Pork" });
  const sent = page.waitForResponse((r) => r.url().includes("/feedback") && r.request().method() === "POST");
  await cards.first().getByRole("button", { name: "Send" }).click();
  const restaurantId = (await (await sent).json()).restaurant_id as number;

  try {
    await expect(page.getByText("Thanks! We'll hide this restaurant from people who avoid Pork.")).toBeVisible();
    // 방금 제보한 가게는 목록에서 빠진다.
    await expect(page.getByRole("listitem").filter({ hasText: name })).toHaveCount(0);
    await expect(page.getByRole("listitem")).toHaveCount(before - 1);

    // 로그인하지 않으면 제보 버튼 대신 로그인 안내가 보인다.
    await page.goto("/");
    await page.getByRole("button", { name: "Log out" }).click();
    await expect(page.getByRole("link", { name: "Log in" })).toBeVisible();
    await page.getByRole("link", { name: "Diet settings" }).click();
    await page.getByRole("button", { name: "Halal" }).click();
    await page.getByRole("button", { name: "Save" }).click();
    await page.getByRole("link", { name: "Other restaurants" }).click();
    await expect(page.getByRole("link", { name: "Log in to report an ingredient." }).first()).toBeVisible();
  } finally {
    // 정리: 로그인했던 세션으로 내 제보를 취소한다(로그아웃했다면 다시 로그인).
    await page.goto("/login");
    await page.getByLabel("Email").fill(EMAIL);
    await page.getByLabel("Password").fill(PASSWORD);
    await page.getByRole("button", { name: "Log in" }).click();
    await expect(page.getByRole("button", { name: "Log out" })).toBeVisible();
    const res = await page.request.delete(`/api/v1/restaurants/${restaurantId}/feedback/pork`);
    expect(res.status()).toBe(204);
  }
});
