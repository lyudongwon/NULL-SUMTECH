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
            <span className="font-semibold">Rule</span> {result.rule} · {RULE_LABEL[result.rule]}
          </p>
          {/* 휴대폰 폭에서 4열이 붙지 않게 재료마다 두 줄로 보인다: 이름·걸리는 항목, 아래에 확인/추정·확신도·출처.
              display를 바꿔도 표로 읽히도록 role을 직접 적는다. 뒤 세 열 이름은 화면 읽기용으로만 둔다. */}
          <table role="table" className="block w-full text-left">
            <thead role="rowgroup" className="block text-xs text-neutral-500">
              <tr role="row" className="flex">
                <th role="columnheader" className="pb-1 font-medium">Ingredient</th>
                <th role="columnheader" className="sr-only">Evidence</th>
                <th role="columnheader" className="sr-only">Confidence</th>
                <th role="columnheader" className="sr-only">Source</th>
              </tr>
            </thead>
            <tbody role="rowgroup" className="block">
              {result.ingredients.map((ing) => (
                <tr
                  key={ing.name}
                  role="row"
                  className="flex flex-wrap items-baseline gap-x-1.5 border-t border-neutral-100 py-2 text-xs text-neutral-500"
                >
                  <td role="cell" className="flex w-full flex-wrap items-baseline justify-between gap-x-3 pb-0.5 text-sm text-neutral-900">
                    <span>{ing.name}</span>
                    {ing.restrictions.length > 0 && (
                      <span className="text-xs font-medium text-red-700">
                        {ing.restrictions.map((code) => itemNames[code] ?? code).join(", ")}
                      </span>
                    )}
                  </td>
                  <td role="cell" className="after:ml-1.5 after:content-['·']">{EVIDENCE_LABEL[ing.evidence]}</td>
                  <td role="cell" className="after:ml-1.5 after:content-['·']">{CONFIDENCE_LABEL[ing.confidence]}</td>
                  <td role="cell">{BASIS_LABEL[ing.basis]}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </li>
  );
}
