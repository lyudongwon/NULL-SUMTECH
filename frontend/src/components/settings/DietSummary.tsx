"use client";

import { useDietSummary } from "@/hooks/useDietSummary";

type Props = { variant: "home" | "result" };

/**
 * 지금 식단 한 줄. home: 첫 화면 "Your diet: …", result: 판정 화면 "Checked for: …" (docs/api.md > 화면 문구)
 * result는 판정 화면 제목줄 안내 문장 안에 들어가서 글자만 돌려준다.
 */
export function DietSummary({ variant }: Props) {
  const summary = useDietSummary();
  if (!summary) return null;

  if (variant === "home") {
    const text =
      summary.status === "none"
        ? "No diet set yet."
        : `Your diet: ${summary.presetName ? `${summary.presetName} (${summary.itemNames.join(", ")})` : summary.itemNames.join(", ")}`;
    return (
      <p className="flex items-center gap-2 rounded-[14px] bg-surface px-4 py-3 text-sm text-body">
        <span className={`h-2 w-2 shrink-0 rounded-full ${summary.status === "set" ? "bg-olive" : "bg-[#b4bac4]"}`} />
        {text}
      </p>
    );
  }

  return <>Checked for: {summary.status === "none" ? "no diet restrictions" : summary.itemNames.join(", ")}</>;
}
