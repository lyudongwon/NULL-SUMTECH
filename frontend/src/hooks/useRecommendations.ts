"use client";

import { useCallback, useEffect, useState } from "react";
import { ApiError } from "@/lib/apiClient";
import { readUserId } from "@/lib/userId";
import { getRecommendations } from "@/services/restaurantService";
import { getPresets } from "@/services/restrictionService";
import type { RestrictionItem } from "@/types/restriction";
import type { Restaurant } from "@/types/restaurant";

type State =
  | { status: "loading" }
  | { status: "needs-settings" }
  | { status: "needs-login" }
  | { status: "error"; message: string }
  | { status: "ready"; restaurants: Restaurant[]; items: RestrictionItem[]; itemNames: Record<string, string> };

/**
 * OPT-2 추천 목록. 사용자 번호가 없거나 서버에 없으면 설정부터 하도록 안내한다.
 * 가입한 계정의 로그인이 풀렸으면(401·403) 로그인하도록 안내한다(OPT-4).
 * reload는 제보(OPT-5)를 보낸 뒤 목록을 다시 받는 데 쓴다.
 */
export function useRecommendations(): State & { reload: () => void } {
  const [state, setState] = useState<State>({ status: "loading" });

  const load = useCallback(() => {
    const userId = readUserId();
    if (!userId) {
      setState({ status: "needs-settings" });
      return;
    }
    Promise.all([getRecommendations(userId), getPresets()])
      .then(([reco, presets]) =>
        setState({
          status: "ready",
          restaurants: reco.restaurants,
          items: presets.items,
          itemNames: Object.fromEntries(presets.items.map((i) => [i.code, i.name])),
        }),
      )
      .catch((error) => {
        if (error instanceof ApiError && (error.status === 401 || error.status === 403))
          setState({ status: "needs-login" });
        else if (error instanceof ApiError && error.status === 404) setState({ status: "needs-settings" });
        else
          setState({
            status: "error",
            message: error instanceof ApiError ? error.detail : "Couldn't load recommendations.",
          });
      });
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return { ...state, reload: load };
}
