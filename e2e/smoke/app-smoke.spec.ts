import { expect, test } from "@playwright/test";

// 스모크 테스트: 앱이 떠서 핵심 화면과 API가 살아 있는지만 빠르게 본다.
// 사진 판독(②)·판정 엔진(③)이 없어도 통과해야 한다. 흐름 확인은 scenarios/가 맡는다.

test("상태 확인과 API 프록시가 응답한다", async ({ request }) => {
  const health = await request.get("/__health");
  expect(health.status()).toBe(200);
  expect(await health.json()).toMatchObject({ status: "ok" });

  const presets = await request.get("/api/v1/restrictions/presets");
  expect(presets.status()).toBe(200);
  const body = await presets.json();
  expect(body.presets.map((p: { name: string }) => p.name)).toEqual(
    expect.arrayContaining(["할랄", "채식"]),
  );
});

test("첫 화면에 서비스 소개와 고지 문구가 보인다", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { level: 1 })).toBeVisible();
  await expect(page.getByRole("link", { name: "사진 올리기" })).toBeVisible();
  await expect(page.getByRole("link", { name: "식단 제한 설정" })).toBeVisible();
  await expect(page.getByText("판정 보조이며, 최종 확인은 본인과 영양사에게.")).toBeVisible();
});

test("식단 제한을 저장하면 다시 열어도 남아 있고, 맞는 음식점이 추천된다", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "식단 제한 설정" }).click();
  await page.getByRole("button", { name: "채식" }).click();
  await page.getByRole("button", { name: "저장" }).click();
  await expect(page.getByRole("link", { name: "사진 올리기" })).toBeVisible();

  await page.getByRole("link", { name: "식단 제한 설정" }).click();
  await expect(page.getByRole("button", { name: "채식" })).toHaveAttribute("aria-pressed", "true");
  await expect(page.getByRole("switch", { name: "고기", exact: true })).toBeChecked();
  await expect(page.getByRole("switch", { name: "해산물", exact: true })).toBeChecked();
  await page.getByRole("link", { name: "처음으로" }).click();

  await page.getByRole("link", { name: "다른 음식점 추천" }).click();
  await expect(page.getByRole("heading", { name: "다른 음식점 추천" })).toBeVisible();
  await expect(page.getByRole("listitem").first()).toBeVisible();
});

test("결과 화면에 빈 메뉴가 오면 '입력 값이 없습니다.' 팝업이 뜬다", async ({ page }) => {
  await page.goto(`/result?menus=${encodeURIComponent(JSON.stringify([""]))}`);
  await expect(page.getByRole("alertdialog")).toContainText("입력 값이 없습니다.");
});
