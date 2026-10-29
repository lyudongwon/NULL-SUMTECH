// 판정 결과 화면의 표시 문구. 코드 값은 docs/api.md 열거형을 따른다.
import type { JudgmentIngredient, JudgmentLevel, JudgmentResult } from "@/hooks/useJudgment";

export const LEVEL_LABEL: Record<JudgmentLevel, string> = {
  SAFE: "안전",
  CHECK: "확인 필요",
  BLOCKED: "불가",
};

export const LEVEL_STYLE: Record<JudgmentLevel, string> = {
  SAFE: "bg-emerald-100 text-emerald-800 border-emerald-300",
  CHECK: "bg-amber-100 text-amber-900 border-amber-300",
  BLOCKED: "bg-red-100 text-red-800 border-red-300",
};

export const EVIDENCE_LABEL: Record<JudgmentIngredient["evidence"], string> = {
  CONFIRMED: "확인",
  INFERRED: "추정",
};

export const CONFIDENCE_LABEL: Record<JudgmentIngredient["confidence"], string> = {
  HIGH: "높음",
  MEDIUM: "보통",
  LOW: "낮음",
};

export const BASIS_LABEL: Record<JudgmentIngredient["basis"], string> = {
  DB: "음식 DB",
  NAME_KEYWORD: "이름 속 재료",
  COOKING_METHOD: "조리법에서 추정",
  RECIPE_SEARCH: "만개의 레시피",
  AI_ESTIMATE: "AI 추정",
};

export const RULE_LABEL: Record<JudgmentResult["rule"], string> = {
  R0: "식단 제한이 없어 안전",
  R1: "확인된 재료가 제한에 걸림",
  R2: "추정 재료가 제한에 걸림",
  R3: "재료를 모두 AI로 추정해 확인 필요",
  R4: "제한에 걸리는 재료가 없음",
};
