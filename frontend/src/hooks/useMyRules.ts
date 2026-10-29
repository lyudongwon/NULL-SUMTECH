"use client";

import { useEffect, useState } from "react";
import { readUserId } from "@/lib/userId";
import { getPresets, getRestrictions } from "@/services/restrictionService";

const CHANGED = "caniet:rules-changed";

/** 설정 화면이 저장에 성공하면 부른다. 머리글의 "My rules"가 다시 불러온다. */
export function notifyRulesChanged() {
  window.dispatchEvent(new Event(CHANGED));
}

/** 저장된 식단 제한을 짧은 이름으로 보여 준다. 예: "Halal", "Meat · Peanut", 저장 전이면 saved=false */
export type MyRules = { saved: boolean; label: string | null };

export function useMyRules(): MyRules {
  const [rules, setRules] = useState<MyRules>({ saved: false, label: null });

  useEffect(() => {
    let cancelled = false;
    async function load() {
      const userId = readUserId();
      if (!userId) return setRules({ saved: false, label: null });
      try {
        const [list, saved] = await Promise.all([getPresets(), getRestrictions(userId)]);
        const preset = list.presets.find((p) => p.code === saved.preset);
        const on = list.items.filter((item) => saved.toggles[item.code]).map((item) => item.name);
        const label = preset
          ? preset.name
          : on.length === 0
            ? "None"
            : on.length > 3
              ? `${on.slice(0, 3).join(" · ")} +${on.length - 3}`
              : on.join(" · ");
        if (!cancelled) setRules({ saved: true, label });
      } catch {
        // 404(저장 전)·네트워크 오류는 머리글에 "저장 전"으로만 보인다.
        if (!cancelled) setRules({ saved: false, label: null });
      }
    }
    load();
    window.addEventListener(CHANGED, load);
    return () => {
      cancelled = true;
      window.removeEventListener(CHANGED, load);
    };
  }, []);

  return rules;
}
