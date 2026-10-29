// docs/scenarios.yaml 흐름 S8 (옛 id signup-login-logout + restaurant-feedback-filter, OPT-4·OPT-5·OPT-2)
// 로그인 전 제보 안내 → 가입 → 제보와 되돌리기(Undo) → 로그아웃 → 다시 로그인했을 때 식단 제한이 남아 있는지 차례로 본다.
// 제보는 끝에서 Undo로 되돌리므로 DB에 남지 않는다.
import { expect, test } from "@playwright/test";

// 같은 DB에서 여러 번 돌려도 겹치지 않게 이메일을 매번 새로 만든다.
const EMAIL = `s8-${Date.now()}@example.com`;
const PASSWORD = "s8-test-password";

test("가입한 사용자가 가게의 돼지고기를 제보하면 할랄 추천에서 빠지고, 다시 로그인해도 식단 제한이 남아 있다", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("button", { name: "Halal" }).click();
  await page.getByRole("button", { name: "Save" }).click();

  // 로그인하지 않으면 제보 버튼 대신 로그인 안내가 보인다.
  await page.getByRole("link", { name: "Other restaurants" }).click();
  await expect(page.getByRole("link", { name: "Log in to report an ingredient." }).first()).toBeVisible();

  await page.getByRole("link", { name: "Sign up" }).click();
  await page.getByLabel("Email").fill(EMAIL);
  await page.getByLabel("Password").fill(PASSWORD);
  await page.getByRole("button", { name: "Sign up" }).click();
  // 가입하면 로그인한 이메일과 "Log out"이 보이고, 가입 전에 저장한 식단이 그대로다.
  await expect(page.getByText(EMAIL)).toBeVisible();
  await expect(page.getByRole("button", { name: "Log out" })).toBeVisible();
  await expect(page.getByText(/Your diet: Halal/)).toBeVisible();

  await page.getByRole("link", { name: "Other restaurants" }).click();
  const cards = page.getByRole("listitem");
  await expect(cards.first()).toBeVisible();
  const before = await cards.count();
  const name = (await cards.first().getByRole("paragraph").first().textContent()) ?? "";
  expect(name).not.toBe("");

  await cards.first().getByRole("button", { name: "Report an ingredient" }).click();
  await cards.first().getByLabel("Which ingredient is used?").selectOption({ label: "Pork" });
  await cards.first().getByRole("button", { name: "Send" }).click();
  await expect(page.getByText("Thanks! We'll hide this restaurant from people who avoid Pork.")).toBeVisible();
  // 방금 제보한 가게는 목록에서 빠진다.
  await expect(page.getByRole("listitem").filter({ hasText: name })).toHaveCount(0);
  await expect(page.getByRole("listitem")).toHaveCount(before - 1);

  // "Undo"로 제보를 되돌리면 그 가게가 다시 보인다.
  await page.getByRole("button", { name: "Undo" }).click();
  await expect(page.getByText("Your report was removed.")).toBeVisible();
  await expect(page.getByRole("listitem").filter({ hasText: name })).toHaveCount(1);
  await expect(page.getByRole("listitem")).toHaveCount(before);

  await page.getByRole("link", { name: "Home" }).click();
  await page.getByRole("button", { name: "Log out" }).click();
  await expect(page.getByRole("link", { name: "Log in" })).toBeVisible();
  await expect(page.getByRole("link", { name: "Sign up" })).toBeVisible();
  await expect(page.getByText("No diet set yet.")).toBeVisible();

  // 틀린 비밀번호는 거절한다.
  await page.getByRole("link", { name: "Log in" }).click();
  await page.getByLabel("Email").fill(EMAIL);
  await page.getByLabel("Password").fill("not-the-password");
  await page.getByRole("button", { name: "Log in" }).click();
  await expect(page.getByText("Incorrect email or password.")).toBeVisible();

  await page.getByLabel("Password").fill(PASSWORD);
  await page.getByRole("button", { name: "Log in" }).click();
  await expect(page.getByRole("button", { name: "Log out" })).toBeVisible();

  await page.getByRole("link", { name: "Diet settings" }).click();
  await expect(page.getByRole("button", { name: "Halal" })).toHaveAttribute("aria-pressed", "true");
});
