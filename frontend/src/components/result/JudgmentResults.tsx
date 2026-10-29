"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { EMPTY_INPUT_MESSAGE, parseMenus, useJudgment } from "@/hooks/useJudgment";
import { ResultCard } from "./ResultCard";

export function JudgmentResults() {
  const router = useRouter();
  const params = useSearchParams();
  const menus = parseMenus(params.get("menus"));
  // ③의 판정 API가 나오기 전 화면 개발용. 개발 서버에서만 ?demo=1을 받는다.
  const demo = process.env.NODE_ENV !== "production" && params.get("demo") === "1";
  const { state, retry } = useJudgment(menus, demo);

  if (state.status === "loading") {
    return <p className="text-neutral-500">Checking…</p>;
  }

  if (state.status === "empty") {
    return (
      <div role="alertdialog" aria-modal="true" aria-labelledby="empty-input" className="fixed inset-0 z-10 flex items-center justify-center bg-black/40 px-6">
        <div className="w-full max-w-xs rounded-xl bg-white p-5 shadow-lg">
          <p id="empty-input" className="mb-4 text-center font-semibold">
            {EMPTY_INPUT_MESSAGE}
          </p>
          <button
            type="button"
            onClick={() => router.push("/scan")}
            className="w-full rounded-lg bg-emerald-600 px-4 py-2 font-semibold text-white"
          >
            OK
          </button>
        </div>
      </div>
    );
  }

  if (state.status === "error") {
    // 판정이 실패해도(AI 지연·서버 오류) 다시 판정하거나 사진을 다시 고를 수 있게 한다.
    return (
      <div className="flex flex-col gap-3">
        <p role="alert" className="text-red-600">
          {state.message}
        </p>
        <div className="flex gap-3">
          <button
            type="button"
            onClick={retry}
            className="rounded-lg bg-emerald-600 px-4 py-2 font-semibold text-white"
          >
            Try again
          </button>
          <Link href="/scan" className="rounded-lg border border-neutral-300 px-4 py-2 font-semibold">
            Retake
          </Link>
        </div>
      </div>
    );
  }

  const noSafe = state.results.every((r) => r.level !== "SAFE");

  return (
    <div className="flex flex-col gap-4">
      <p className="text-sm text-neutral-600">Tap a menu to see why.</p>
      <ul className="flex flex-col gap-3">
        {state.results.map((result) => (
          <ResultCard key={result.menu_name} result={result} itemNames={state.itemNames} />
        ))}
      </ul>
      {noSafe && (
        <Link
          href="/restaurants"
          className="rounded-lg border border-emerald-600 px-4 py-3 text-center font-semibold text-emerald-700"
        >
          Other restaurants
        </Link>
      )}
    </div>
  );
}
