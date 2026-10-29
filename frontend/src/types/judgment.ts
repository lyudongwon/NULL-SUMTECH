// REQ-3 3단계 판정·REQ-6 근거 상세, OPT-1 수동 입력 판정, REQ-5 재료 찾기 (docs/api.md). 역할 ③이 정한다.

export type Level = "SAFE" | "CHECK" | "BLOCKED";

/** CONFIRMED = 확인(음식 DB에 등록된 재료), INFERRED = 추정 */
export type Evidence = "CONFIRMED" | "INFERRED";

export type Confidence = "HIGH" | "MEDIUM" | "LOW";

/** 재료를 어디서 찾았는지. AI_ESTIMATE는 항상 evidence가 INFERRED다 */
export type Basis = "DB" | "NAME_KEYWORD" | "COOKING_METHOD" | "RECIPE_SEARCH" | "AI_ESTIMATE";

/** 적용된 판정 규칙. docs/api.md > REQ-3 > 판정 규칙 */
export type Rule = "R0" | "R1" | "R2" | "R3" | "R4";

export type JudgmentIngredient = {
  name: string;
  evidence: Evidence;
  confidence: Confidence;
  basis: Basis;
  /** 재료에 정해진 제한 항목 코드 전부(사용자와 상관없다). 걸리는 항목이 없으면 [] */
  restrictions: string[];
};

export type JudgmentResult = {
  menu_name: string;
  level: Level;
  /** 배지 옆에 보이는 근거 한 줄 */
  reason: string;
  /** CONFIRMED 재료로 걸린 제한 항목(BLOCKED의 근거). 그 밖에는 [] */
  matched_restrictions: string[];
  rule: Rule;
  /** 근거 상세(REQ-6)로 보여 줄 재료 목록 */
  ingredients: JudgmentIngredient[];
};

/** POST /judgments 응답. results는 보낸 menu_names와 같은 순서다 */
export type JudgmentResponse = { results: JudgmentResult[] };

/** POST /ingredients/infer 응답. 재료를 하나도 찾지 못하면 ingredients가 [] */
export type IngredientInferResponse = {
  menu_name: string;
  ingredients: JudgmentIngredient[];
};
