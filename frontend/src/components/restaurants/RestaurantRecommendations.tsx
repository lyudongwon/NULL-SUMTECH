"use client";

import Link from "next/link";
import { useRecommendations } from "@/hooks/useRecommendations";

export function RestaurantRecommendations() {
  const state = useRecommendations();

  if (state.status === "loading") {
    return <p className="text-neutral-500">불러오는 중…</p>;
  }
  if (state.status === "needs-settings") {
    return (
      <div className="flex flex-col gap-3">
        <p>먼저 식단 제한을 설정해 주세요.</p>
        <Link href="/settings" className="self-start rounded-lg bg-emerald-600 px-4 py-2 font-semibold text-white">
          식단 제한 설정
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
  if (state.restaurants.length === 0) {
    return <p>내 식단 제한에 맞는 음식점이 아직 없어요.</p>;
  }

  return (
    <ul className="flex flex-col gap-3">
      {state.restaurants.map((r) => (
        <li key={r.restaurant_id} className="rounded-lg border border-neutral-200 p-4">
          <p className="font-semibold">{r.name}</p>
          <p className="text-sm text-neutral-600">
            {[r.category, r.address].filter(Boolean).join(" · ")}
          </p>
          {r.matched_restrictions.length > 0 && (
            <p className="mt-2 text-xs text-emerald-700">
              {r.matched_restrictions.map((code) => state.itemNames[code] ?? code).join(", ")} 없음
            </p>
          )}
        </li>
      ))}
    </ul>
  );
}
