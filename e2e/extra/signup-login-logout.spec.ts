// 회원가입·로그인·로그아웃(OPT-4) 확인. docs/problem.yaml > features > OPT-4 > done
// 키트의 흐름은 3~8개라 scenarios.yaml 흐름(S1~S8)에 넣지 않고 e2e/extra에서 돌린다(채점기는 돌리지 않는다).
// 가입 전에 저장한 식단 제한이 계정에 남고, 로그아웃했다가 다시 로그인하면 불러와지는지 본다.
import { expect, test } from "@playwright/test";

// 같은 DB에서 여러 번 돌려도 겹치지 않게 이메일을 매번 새로 만든다.
const EMAIL = `s9-${Date.now()}@example.com`;
const PASSWORD = "s9-test-password";

test("가입하면 식단 제한이 계정에 남고, 로그아웃했다가 다시 로그인해도 불러와진다", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Diet settings" }).click();
  await page.getByRole("button", { name: "Halal" }).click();
  await page.getByRole("button", { name: "Save" }).click();

  await page.getByRole("link", { name: "Sign up" }).click();
  await page.getByLabel("Email").fill(EMAIL);
  await page.getByLabel("Password").fill(PASSWORD);
  await page.getByRole("button", { name: "Sign up" }).click();

  // 가입하면 첫 화면에 로그인한 이메일과 "Log out"이 보이고, 가입 전에 저장한 식단이 그대로다.
  await expect(page.getByText(EMAIL)).toBeVisible();
  await expect(page.getByRole("button", { name: "Log out" })).toBeVisible();
  await expect(page.getByText(/Your diet: Halal/)).toBeVisible();

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
