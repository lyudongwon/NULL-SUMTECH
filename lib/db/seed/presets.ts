// REQ-4 제한 항목·프리셋 시드. 코드는 docs/api.md > REQ-4 식단 제한 프리셋 목록 조회를 따른다.
import type { Db } from "../index";
import { presets, restrictionItems } from "../schema";

export const RESTRICTION_ITEMS = [
  { code: "meat", name: "고기" },
  { code: "pork", name: "돼지고기" },
  { code: "beef", name: "소고기" },
  { code: "seafood", name: "해산물" },
  { code: "egg", name: "계란" },
  { code: "dairy", name: "우유·유제품" },
  { code: "peanut", name: "땅콩" },
  { code: "alcohol", name: "술" },
] as const;

export const PRESETS = [
  { code: "HALAL", name: "할랄", items: ["pork", "alcohol"] },
  { code: "VEGAN", name: "비건", items: ["meat", "seafood", "egg", "dairy"] },
];

export async function seedPresets(db: Db) {
  await db
    .insert(restrictionItems)
    .values(RESTRICTION_ITEMS.map((item, i) => ({ ...item, sortOrder: i })))
    .onConflictDoNothing();
  await db
    .insert(presets)
    .values(PRESETS.map((preset, i) => ({ ...preset, sortOrder: i })))
    .onConflictDoNothing();
}
