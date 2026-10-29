"use client";

import { useEffect, useState } from "react";
import { ApiError } from "@/lib/apiClient";
import { readUserId } from "@/lib/userId";
import { getRecommendations } from "@/services/restaurantService";
import { getPresets } from "@/services/restrictionService";
import type { Restaurant } from "@/types/restaurant";

type State =
  | { status: "loading" }
  | { status: "needs-settings" }
  | { status: "error"; message: string }
  | { status: "ready"; restaurants: Restaurant[]; itemNames: Record<string, string> };

/** OPT-2 추천 목록. 사용자 번호가 없거나 서버에 없으면 설정부터 하도록 안내한다. */
export function useRecommendations(): State {
  const [state, setState] = useState<State>({ status: "loading" });

  useEffect(() => {
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
          itemNames: Object.fromEntries(presets.items.map((i) => [i.code, i.name])),
        }),
      )
      .catch((error) => {
        if (error instanceof ApiError && error.status === 404) setState({ status: "needs-settings" });
        else setState({ status: "error", message: error instanceof ApiError ? error.detail : "Couldn't load recommendations." });
      });
  }, []);

  return state;
}
