"use client";

import { useState } from "react";
import type { JudgmentResult } from "@/hooks/useJudgment";
import {
  BASIS_LABEL,
  CONFIDENCE_LABEL,
  EVIDENCE_LABEL,
  LEVEL_LABEL,
  LEVEL_STYLE,
  RULE_LABEL,
} from "./labels";

type Props = { result: JudgmentResult; itemNames: Record<string, string> };

/** 메뉴 하나의 배지·근거 한 줄(REQ-3). 누르면 근거 상세(REQ-6)가 펼쳐진다. */
export function ResultCard({ result, itemNames }: Props) {
  const [open, setOpen] = useState(false);
  const detailId = `detail-${result.menu_name}`;

  return (
    <li className="rounded-lg border border-neutral-200">
      <button
        type="button"
        aria-expanded={open}
        aria-controls={detailId}
        onClick={() => setOpen((v) => !v)}
        className="flex w-full flex-col gap-1 p-4 text-left"
      >
        <span className="flex items-center justify-between gap-3">
          <span className="text-lg font-semibold">{result.menu_name}</span>
          <span className={`shrink-0 rounded-full border px-3 py-1 text-sm font-semibold ${LEVEL_STYLE[result.level]}`}>
            {LEVEL_LABEL[result.level]}
          </span>
        </span>
        <span data-testid="reason" className="text-sm text-neutral-700">{result.reason}</span>
      </button>

      {open && (
        <div id={detailId} className="border-t border-neutral-200 p-4 text-sm">
          <p className="mb-3">
            <span className="font-semibold">적용된 규칙</span> {result.rule} · {RULE_LABEL[result.rule]}
          </p>
          <table className="w-full text-left">
            <thead className="text-xs text-neutral-500">
              <tr>
                <th className="pb-1 font-medium">재료</th>
                <th className="pb-1 font-medium">구분</th>
                <th className="pb-1 font-medium">확신도</th>
                <th className="pb-1 font-medium">출처</th>
              </tr>
            </thead>
            <tbody>
              {result.ingredients.map((ing) => (
                <tr key={ing.name} className="border-t border-neutral-100 align-top">
                  <td className="py-1.5">
                    {ing.name}
                    {ing.restrictions.length > 0 && (
                      <span className="block text-xs text-red-700">
                        {ing.restrictions.map((code) => itemNames[code] ?? code).join(", ")}
                      </span>
                    )}
                  </td>
                  <td className="py-1.5">{EVIDENCE_LABEL[ing.evidence]}</td>
                  <td className="py-1.5">{CONFIDENCE_LABEL[ing.confidence]}</td>
                  <td className="py-1.5">{BASIS_LABEL[ing.basis]}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </li>
  );
}
