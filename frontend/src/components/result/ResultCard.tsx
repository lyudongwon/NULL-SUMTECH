"use client";

import { useState } from "react";
import { LevelBadge } from "@/components/ui/LevelBadge";
import type { JudgmentResult, Level } from "@/types/judgment";
import { BASIS_LABEL, CONFIDENCE_LABEL, EVIDENCE_LABEL, RULE_LABEL } from "./labels";

type Props = { result: JudgmentResult; itemNames: Record<string, string> };

const ACCENT: Record<Level, string> = {
  SAFE: "border-l-olive",
  CHECK: "border-l-check",
  BLOCKED: "border-l-avoid",
};

/**
 * 메뉴 하나의 배지·근거 한 줄(REQ-3). 누르면 근거 상세(REQ-6)가 같은 카드 안에 펼쳐진다.
 * E2E가 카드(listitem) 안에서 재료 표·"Rule"을 찾으므로 상세를 카드 밖으로 빼지 않는다.
 */
export function ResultCard({ result, itemNames }: Props) {
  const [open, setOpen] = useState(false);
  const detailId = `detail-${result.menu_name}`;

  return (
    <li className={`border-b border-l-[3px] border-b-divider last:border-b-0 ${open ? `bg-bg ${ACCENT[result.level]}` : "border-l-transparent"}`}>
      <button
        type="button"
        aria-expanded={open}
        aria-controls={detailId}
        onClick={() => setOpen((v) => !v)}
        className="flex w-full items-center gap-3.5 px-[18px] py-3.5 text-left hover:bg-[#fafbfc]"
      >
        <span className="flex min-w-0 flex-1 flex-col gap-[3px]">
          <span className="text-base font-semibold">{result.menu_name}</span>
          <span data-testid="reason" className="text-[13px] text-body">
            {result.reason}
          </span>
        </span>
        <LevelBadge level={result.level} />
      </button>

      {open && (
        <div id={detailId} className="flex flex-col gap-3 px-[18px] pb-[18px]">
          <p className="text-[13px] text-body">
            <span className="font-semibold text-ink">Rule</span>{" "}
            <span className="font-mono">{result.rule}</span> · {RULE_LABEL[result.rule]}
          </p>
          <div className="overflow-x-auto rounded-[14px] border border-divider bg-surface">
            <table className="w-full text-left text-sm">
              <thead className="text-xs text-muted">
                <tr>
                  <th className="px-3.5 py-2.5 font-semibold">Ingredient</th>
                  <th className="px-2 py-2.5 font-semibold">Evidence</th>
                  <th className="px-2 py-2.5 font-semibold">Confidence</th>
                  <th className="px-3.5 py-2.5 font-semibold">Source</th>
                </tr>
              </thead>
              <tbody>
                {result.ingredients.map((ing) => (
                  <tr key={ing.name} className="border-t border-divider align-top">
                    <td className="px-3.5 py-2.5 font-semibold">
                      {ing.name}
                      {ing.restrictions.length > 0 && (
                        // 재료에 걸리는 항목 전부라서(내 제한과 상관없음) 빨간색이 아닌 중립색으로 둔다.
                        <span className="mt-1 block w-fit rounded-md bg-divider px-[7px] py-px text-xs font-medium text-body">
                          {ing.restrictions.map((code) => itemNames[code] ?? code).join(", ")}
                        </span>
                      )}
                    </td>
                    <td className="px-2 py-2.5">
                      <span
                        className={`rounded-md px-1.5 text-xs ${
                          ing.evidence === "INFERRED" ? "border border-dashed border-[#858b9a]" : "bg-divider py-px"
                        }`}
                      >
                        {EVIDENCE_LABEL[ing.evidence]}
                      </span>
                    </td>
                    <td className="px-2 py-2.5">{CONFIDENCE_LABEL[ing.confidence]}</td>
                    <td className="px-3.5 py-2.5 text-body">{BASIS_LABEL[ing.basis]}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </li>
  );
}
