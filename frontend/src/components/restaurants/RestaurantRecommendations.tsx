"use client";

import Link from "next/link";
import { Icon } from "@/components/ui/icons";
import { btnPrimary, card } from "@/components/ui/styles";
import { useRecommendations } from "@/hooks/useRecommendations";

export function RestaurantRecommendations() {
  const state = useRecommendations();

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
  if (state.status === "error") {
    return (
      <p role="alert" className="rounded-[14px] bg-avoid-bg px-4 py-3 text-sm font-medium text-avoid">
        {state.message}
      </p>
    );
  }
  if (state.restaurants.length === 0) {
    return <p className={`${card} p-6 text-[15px] text-body`}>No restaurants match your diet yet.</p>;
  }

  // 카드 안 첫 두 문단은 이름·종류 순서로 둔다(E2E S8이 순서로 찾는다).
  return (
    <ul className="grid grid-cols-1 gap-4 sm:grid-cols-[repeat(auto-fill,minmax(280px,1fr))]">
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
        </li>
      ))}
    </ul>
  );
}
