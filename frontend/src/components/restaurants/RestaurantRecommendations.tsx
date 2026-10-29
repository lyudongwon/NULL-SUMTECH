"use client";

import Link from "next/link";
import { useRecommendations } from "@/hooks/useRecommendations";

export function RestaurantRecommendations() {
  const state = useRecommendations();

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
  if (state.status === "error") {
    return (
      <p role="alert" className="text-red-600">
        {state.message}
      </p>
    );
  }
  if (state.restaurants.length === 0) {
    return <p>No restaurants match your diet yet.</p>;
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
              Free of {r.matched_restrictions.map((code) => state.itemNames[code] ?? code).join(", ")}
            </p>
          )}
        </li>
      ))}
    </ul>
  );
}
