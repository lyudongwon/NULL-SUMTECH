"use client";

import { useEffect, useState } from "react";
import { ApiError } from "@/lib/apiClient";
import { readUserId } from "@/lib/userId";
import { getPresets, getRestrictions } from "@/services/restrictionService";

const CHANGED = "caniet:diet-changed";

/** 설정 화면이 저장에 성공하면 부른다. 화면 전체에 남는 머리글의 "My rules"가 다시 불러온다. */
export function notifyDietChanged() {
  window.dispatchEvent(new Event(CHANGED));
}

/** 저장한 식단 제한 요약. none: 아직 저장하지 않았거나 켠 항목이 없음, null: 불러오는 중이거나 실패(아무것도 안 보임) */
export type DietSummary = { status: "none" } | { status: "set"; presetName: string | null; itemNames: string[] };

/** 머리글·첫 화면·결과 화면에 지금 식단을 보여 주려고 읽기만 한다. user_id를 새로 발급하지 않는다. */
export function useDietSummary(): DietSummary | null {
  const [summary, setSummary] = useState<DietSummary | null>(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      const userId = readUserId();
      if (!userId) {
        setSummary({ status: "none" });
        return;
      }
      try {
        const [list, saved] = await Promise.all([getPresets(), getRestrictions(userId)]);
        if (cancelled) return;
        const itemNames = list.items.filter((item) => saved.toggles[item.code]).map((item) => item.name);
        const presetName = list.presets.find((p) => p.code === saved.preset)?.name ?? null;
        setSummary(itemNames.length === 0 ? { status: "none" } : { status: "set", presetName, itemNames });
      } catch (error) {
        if (cancelled) return;
        // 404: 서버에 저장한 적 없음. 그 밖의 오류는 요약을 숨긴다.
        setSummary(error instanceof ApiError && error.status === 404 ? { status: "none" } : null);
      }
    }
    load();
    window.addEventListener(CHANGED, load);
    return () => {
      cancelled = true;
      window.removeEventListener(CHANGED, load);
    };
  }, []);

  return summary;
}
