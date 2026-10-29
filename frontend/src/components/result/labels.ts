// 판정 결과 화면의 표시 문구. 코드 값은 docs/api.md 열거형을 따른다.
import type { JudgmentIngredient, JudgmentResult } from "@/types/judgment";

export const EVIDENCE_LABEL: Record<JudgmentIngredient["evidence"], string> = {
  CONFIRMED: "Confirmed",
  INFERRED: "Estimated",
};

export const CONFIDENCE_LABEL: Record<JudgmentIngredient["confidence"], string> = {
  HIGH: "High",
  MEDIUM: "Medium",
  LOW: "Low",
};

export const BASIS_LABEL: Record<JudgmentIngredient["basis"], string> = {
  DB: "Food database",
  NAME_KEYWORD: "Menu name",
  COOKING_METHOD: "Cooking method",
  RECIPE_SEARCH: "10000recipe",
  AI_ESTIMATE: "AI estimate",
};

export const RULE_LABEL: Record<JudgmentResult["rule"], string> = {
  R0: "No diet restrictions set",
  R1: "A confirmed ingredient is restricted",
  R2: "An estimated ingredient may be restricted",
  R3: "All ingredients are AI estimates",
  R4: "No restricted ingredients found",
};
