"use client";

import Link from "next/link";
import { useState } from "react";
import { Icon } from "@/components/ui/icons";
import { btnOutline, btnPrimary, card } from "@/components/ui/styles";
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
    return <p className="text-body">Loading…</p>;
  }
  if (state.status === "needs-settings") {
    return (
      <div className={`${card} flex flex-col items-start gap-4 p-6`}>
        <p className="text-[15px]">Please set your diet first.</p>
        <Link href="/settings" className={btnPrimary}>
          Diet settings
        </Link>
      </div>
    );
  }
  if (state.status === "needs-login") {
    return (
      <div className={`${card} flex flex-col items-start gap-4 p-6`}>
        <p className="text-[15px]">Please log in.</p>
        <Link href="/login" className={btnPrimary}>
          Log in
        </Link>
      </div>
    );
  }
  if (state.status === "error") {
    return (
      <p role="alert" className="rounded-[14px] bg-avoid-bg px-4 py-3 text-sm font-medium text-avoid">
        {state.message}
      </p>
    );
  }

  const thanks = notice && (
    <div role="status" className="flex items-center justify-between gap-3 rounded-[14px] bg-safe-bg px-4 py-3 text-sm text-safe">
      <span className="font-medium">{notice.text}</span>
      {notice.undo && (
        <button
          type="button"
          onClick={() => undo(notice.undo!.restaurantId, notice.undo!.itemCode)}
          className="shrink-0 rounded-[10px] border border-safe px-3 py-1 font-semibold"
        >
          Undo
        </button>
      )}
    </div>
  );

  if (state.restaurants.length === 0) {
    return (
      <div className="flex flex-col gap-4">
        {thanks}
        <p className={`${card} p-6 text-[15px] text-body`}>No restaurants match your diet yet.</p>
      </div>
    );
  }

  // 카드 안 첫 두 문단은 이름·종류 순서로 둔다(E2E S8·S10이 순서로 찾는다).
  return (
    <div className="flex flex-col gap-4">
      {thanks}
      <ul className="grid grid-cols-1 items-start gap-4 sm:grid-cols-[repeat(auto-fill,minmax(280px,1fr))]">
        {state.restaurants.map((r) => (
          <li key={r.restaurant_id} className={`${card} flex flex-col gap-1.5 px-[18px] pt-4 pb-[18px]`}>
            <p className="text-[17px] font-semibold">{r.name}</p>
            <p className="text-[13px] text-muted">{[r.category, r.address].filter(Boolean).join(" · ")}</p>
            {r.matched_restrictions.length > 0 && (
              <p className="mt-1 inline-flex items-center gap-1 self-start rounded-full bg-safe-bg py-[3px] pr-[9px] pl-[7px] text-[12.5px] font-semibold text-safe">
                <Icon name="check" size={11} stroke={3.2} />
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
              <Link href="/login" className={`${btnOutline} mt-3 h-10 self-start rounded-[10px] px-3.5 text-[13px]`}>
                Log in to report an ingredient.
              </Link>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
