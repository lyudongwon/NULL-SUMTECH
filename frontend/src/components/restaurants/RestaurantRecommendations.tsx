"use client";

import Link from "next/link";
import { useState } from "react";
import { useAccount } from "@/hooks/useAccount";
import { useRecommendations } from "@/hooks/useRecommendations";
import { ApiError } from "@/lib/apiClient";
import { cancelFeedback } from "@/services/restaurantService";
import { FeedbackForm } from "./FeedbackForm";

export function RestaurantRecommendations() {
  const state = useRecommendations();
  const { account } = useAccount();
  // 제보를 보내면 그 가게가 목록에서 빠질 수 있어서, 감사 문구는 카드가 아니라 목록 위에 둔다.
  const [notice, setNotice] = useState<{ text: string; undo?: { restaurantId: number; itemCode: string } } | null>(
    null,
  );

  async function undo(restaurantId: number, itemCode: string) {
    try {
      await cancelFeedback(restaurantId, itemCode);
      setNotice({ text: "Your report was removed." });
      state.reload();
    } catch (error) {
      setNotice({ text: error instanceof ApiError ? error.detail : "Couldn't remove your report." });
    }
  }

  if (state.status === "loading") {
    return <p className="text-neutral-500">Loading…</p>;
  }
  if (state.status === "needs-settings") {
    return (
      <div className="flex flex-col gap-3">
        <p>Please set your diet first.</p>
        <Link href="/settings" className="self-start rounded-lg bg-emerald-600 px-4 py-2 font-semibold text-white">
          Diet settings
        </Link>
      </div>
    );
  }
  if (state.status === "needs-login") {
    return (
      <div className="flex flex-col gap-3">
        <p>Please log in.</p>
        <Link href="/login" className="self-start rounded-lg bg-emerald-600 px-4 py-2 font-semibold text-white">
          Log in
        </Link>
      </div>
    );
  }
  if (state.status === "error") {
    return (
      <p role="alert" className="text-red-600">
        {state.message}
      </p>
    );
  }
  const thanks = notice && (
    <div
      role="status"
      className="flex items-center justify-between gap-3 rounded-lg bg-emerald-50 px-4 py-3 text-sm text-emerald-800"
    >
      <span>{notice.text}</span>
      {notice.undo && (
        <button
          type="button"
          onClick={() => undo(notice.undo!.restaurantId, notice.undo!.itemCode)}
          className="shrink-0 rounded-lg border border-emerald-700 px-3 py-1 font-semibold"
        >
          Undo
        </button>
      )}
    </div>
  );
  if (state.restaurants.length === 0) {
    return (
      <div className="flex flex-col gap-3">
        {thanks}
        <p>No restaurants match your diet yet.</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-3">
      {thanks}
      <ul className="flex flex-col gap-3">
        {state.restaurants.map((r) => (
          <li key={r.restaurant_id} className="rounded-lg border border-neutral-200 p-4">
            <p className="font-semibold">{r.name}</p>
            <p className="text-sm text-neutral-600">{[r.category, r.address].filter(Boolean).join(" · ")}</p>
            {r.matched_restrictions.length > 0 && (
              <p className="mt-2 text-xs text-emerald-700">
                Free of {r.matched_restrictions.map((code) => state.itemNames[code] ?? code).join(", ")}
              </p>
            )}
            {account.status === "authenticated" && (
              <FeedbackForm
                restaurantId={r.restaurant_id}
                items={state.items}
                onSent={(item) => {
                  setNotice({
                    text: `Thanks! We'll hide this restaurant from people who avoid ${item.name}.`,
                    undo: { restaurantId: r.restaurant_id, itemCode: item.code },
                  });
                  state.reload();
                }}
              />
            )}
            {account.status === "anonymous" && (
              <Link href="/login" className="mt-3 inline-block text-sm font-semibold text-emerald-700">
                Log in to report an ingredient.
              </Link>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
