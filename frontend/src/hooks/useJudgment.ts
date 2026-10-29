"use client";

import { useEffect, useState } from "react";
import { ApiError, apiRequest } from "@/lib/apiClient";
import { getPresets } from "@/services/restrictionService";
import { ensureUserId } from "./userIdentity";

// TODO(③): ③이 src/types/judgment.ts와 src/services/judgmentService.ts를 올리면
// 아래 타입과 requestJudgments를 그 파일의 것으로 바꾼다(docs/roles.md 만나는 지점 2).
// 지금은 docs/api.md REQ-3 응답을 그대로 옮겨 둔 임시본이다.
export type JudgmentLevel = "SAFE" | "CHECK" | "BLOCKED";
export type JudgmentIngredient = {
  name: string;
  evidence: "CONFIRMED" | "INFERRED";
  confidence: "HIGH" | "MEDIUM" | "LOW";
  basis: "DB" | "NAME_KEYWORD" | "COOKING_METHOD" | "RECIPE_SEARCH" | "AI_ESTIMATE";
  restrictions: string[];
};
export type JudgmentResult = {
  menu_name: string;
  level: JudgmentLevel;
  reason: string;
  matched_restrictions: string[];
  rule: "R0" | "R1" | "R2" | "R3" | "R4";
  ingredients: JudgmentIngredient[];
};
type JudgmentResponse = { results: JudgmentResult[] };

function requestJudgments(userId: number, menuNames: string[]) {
  return apiRequest<JudgmentResponse>("/judgments", {
    method: "POST",
    body: JSON.stringify({ user_id: userId, menu_names: menuNames }),
  });
}

export const EMPTY_INPUT_MESSAGE = "입력 값이 없습니다.";

/** /result?menus=<JSON 배열>의 메뉴 이름을 읽는다. 비었거나 형식이 틀리면 빈 배열. */
export function parseMenus(raw: string | null): string[] {
  if (!raw) return [];
  try {
    const value: unknown = JSON.parse(raw);
    if (!Array.isArray(value)) return [];
    const names = value.filter((v): v is string => typeof v === "string").map((v) => v.trim());
    // 빈 이름이 하나라도 있으면 api.md REQ-3처럼 '입력 값이 없습니다.'로 다룬다.
    return names.length === value.length && names.every(Boolean) ? names : [];
  } catch {
    return [];
  }
}

type State =
  | { status: "loading" }
  | { status: "empty" }
  | { status: "error"; message: string }
  | { status: "ready"; results: JudgmentResult[]; itemNames: Record<string, string> };

/** REQ-3·REQ-6 결과 화면 상태. demo가 true면 개발용 예시 데이터를 쓴다(③의 API가 나오기 전). */
export function useJudgment(menuNames: string[], demo: boolean): State {
  const [state, setState] = useState<State>({ status: "loading" });
  const key = JSON.stringify(menuNames);

  useEffect(() => {
    const names: string[] = JSON.parse(key);
    if (names.length === 0) {
      setState({ status: "empty" });
      return;
    }
    let cancelled = false;
    (async () => {
      try {
        const [presets, response] = await Promise.all([
          getPresets(),
          demo ? Promise.resolve(demoResponse(names)) : ensureUserId().then((id) => requestJudgments(id, names)),
        ]);
        if (cancelled) return;
        setState({
          status: "ready",
          results: response.results,
          itemNames: Object.fromEntries(presets.items.map((i) => [i.code, i.name])),
        });
      } catch (error) {
        if (cancelled) return;
        if (error instanceof ApiError && error.status === 400 && error.detail === EMPTY_INPUT_MESSAGE) {
          setState({ status: "empty" });
        } else {
          setState({ status: "error", message: error instanceof ApiError ? error.detail : "판정을 불러오지 못했습니다." });
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [key, demo]);

  return state;
}

// 개발용 예시(api.md REQ-3 예시를 바탕으로 함). 운영 빌드에서는 쓰지 않는다.
function demoResponse(names: string[]): JudgmentResponse {
  const samples: JudgmentResult[] = [
    {
      menu_name: "",
      level: "BLOCKED",
      reason: "소고기가 들어 있어 채식 기준에 맞지 않습니다.",
      matched_restrictions: ["meat"],
      rule: "R1",
      ingredients: [
        { name: "소고기", evidence: "CONFIRMED", confidence: "HIGH", basis: "DB", restrictions: ["beef", "meat"] },
        { name: "사골 육수", evidence: "INFERRED", confidence: "MEDIUM", basis: "COOKING_METHOD", restrictions: ["beef", "meat"] },
      ],
    },
    {
      menu_name: "",
      level: "CHECK",
      reason: "멸치 육수를 썼을 수 있어 확인이 필요합니다.",
      matched_restrictions: [],
      rule: "R2",
      ingredients: [
        { name: "콩나물", evidence: "CONFIRMED", confidence: "HIGH", basis: "DB", restrictions: [] },
        { name: "멸치 육수", evidence: "INFERRED", confidence: "MEDIUM", basis: "COOKING_METHOD", restrictions: ["seafood"] },
      ],
    },
    {
      menu_name: "",
      level: "SAFE",
      reason: "제한에 걸리는 재료가 없습니다.",
      matched_restrictions: [],
      rule: "R4",
      ingredients: [{ name: "양상추", evidence: "CONFIRMED", confidence: "HIGH", basis: "DB", restrictions: [] }],
    },
  ];
  return { results: names.map((name, i) => ({ ...samples[i % samples.length], menu_name: name })) };
}
