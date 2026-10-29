"use client";

import Link from "next/link";
import { useState } from "react";
import { useAccount } from "@/hooks/useAccount";
import { useRecommendations } from "@/hooks/useRecommendations";
import { FeedbackForm } from "./FeedbackForm";

export function RestaurantRecommendations() {
  const state = useRecommendations();
  const { account } = useAccount();
  // 제보를 보내면 그 가게가 목록에서 빠질 수 있어서, 감사 문구는 카드가 아니라 목록 위에 둔다.
  const [notice, setNotice] = useState<string | null>(null);

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
    <p role="status" className="rounded-lg bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
      {notice}
    </p>
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
                onSent={(itemName) => {
                  setNotice(`Thanks! We'll hide this restaurant from people who avoid ${itemName}.`);
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
