"use client";

import { useDietSummary } from "@/hooks/useDietSummary";

type Props = { variant: "home" | "result" };

/** 지금 식단 한 줄. home: 첫 화면 "Your diet: …", result: 판정 화면 "Checked for: …" (docs/api.md > 화면 문구) */
export function DietSummary({ variant }: Props) {
  const summary = useDietSummary();
  if (!summary) return null;

  if (variant === "home") {
    const text =
      summary.status === "none"
        ? "No diet set yet."
        : `Your diet: ${summary.presetName ? `${summary.presetName} (${summary.itemNames.join(", ")})` : summary.itemNames.join(", ")}`;
    return <p className="rounded-lg bg-neutral-100 px-4 py-3 text-sm text-neutral-700">{text}</p>;
  }

  return (
    <p className="text-sm text-neutral-600">
      Checked for: {summary.status === "none" ? "no diet restrictions" : summary.itemNames.join(", ")}
    </p>
  );
}
