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
          {/* 휴대폰 폭에서는 4열이 붙지 않게 재료마다 두 줄로 보인다: 이름·걸리는 항목, 아래에 확인/추정·확신도·출처.
              넓은 화면(sm 이상)에서는 보통 표로 돌아간다. display를 바꿔도 표로 읽히도록 role을 직접 적고,
              휴대폰 폭에서 뒤 세 열 이름은 화면 읽기용으로만 둔다. */}
          <div className="rounded-[14px] border border-divider bg-surface">
            <table role="table" className="block w-full text-left text-sm sm:table">
              <thead role="rowgroup" className="block text-xs text-muted sm:table-header-group">
                <tr role="row" className="flex sm:table-row">
                  <th role="columnheader" className="px-3.5 py-2.5 font-semibold">Ingredient</th>
                  <th role="columnheader" className="sr-only sm:not-sr-only sm:px-2 sm:py-2.5 sm:font-semibold">Evidence</th>
                  <th role="columnheader" className="sr-only sm:not-sr-only sm:px-2 sm:py-2.5 sm:font-semibold">Confidence</th>
                  <th role="columnheader" className="sr-only sm:not-sr-only sm:px-3.5 sm:py-2.5 sm:font-semibold">Source</th>
                </tr>
              </thead>
              <tbody role="rowgroup" className="block sm:table-row-group">
                {result.ingredients.map((ing) => (
                  <tr
                    key={ing.name}
                    role="row"
                    className="flex flex-wrap items-baseline gap-x-1.5 border-t border-divider px-3.5 py-2.5 text-xs text-body sm:table-row sm:px-0 sm:py-0 sm:align-top sm:text-sm"
                  >
                    <td
                      role="cell"
                      className="flex w-full flex-wrap items-baseline justify-between gap-x-3 pb-1 text-sm font-semibold text-ink sm:table-cell sm:w-auto sm:px-3.5 sm:py-2.5"
                    >
                      <span>{ing.name}</span>
                      {ing.restrictions.length > 0 && (
                        // 재료에 걸리는 항목 전부라서(내 제한과 상관없음) 빨간색이 아닌 중립색으로 둔다.
                        <span className="w-fit rounded-md bg-divider px-[7px] py-px text-xs font-medium text-body sm:mt-1 sm:block">
                          {ing.restrictions.map((code) => itemNames[code] ?? code).join(", ")}
                        </span>
                      )}
                    </td>
                    <td role="cell" className="after:ml-1.5 after:content-['·'] sm:table-cell sm:px-2 sm:py-2.5 sm:after:content-none">
                      <span
                        className={`rounded-md px-1.5 text-xs ${
                          ing.evidence === "INFERRED" ? "border border-dashed border-[#858b9a]" : "bg-divider py-px"
                        }`}
                      >
                        {EVIDENCE_LABEL[ing.evidence]}
                      </span>
                    </td>
                    <td role="cell" className="after:ml-1.5 after:content-['·'] sm:table-cell sm:px-2 sm:py-2.5 sm:after:content-none">
                      {CONFIDENCE_LABEL[ing.confidence]}
                    </td>
                    <td role="cell" className="sm:table-cell sm:px-3.5 sm:py-2.5">
                      {BASIS_LABEL[ing.basis]}
                    </td>
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
