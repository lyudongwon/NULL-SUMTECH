"use client";

import { useScan } from "@/hooks/useScan";
import { ManualMenuInput } from "./ManualMenuInput";
import { PhotoPicker } from "./PhotoPicker";

const PROGRESS = {
  uploading: "사진을 올리는 중…",
  reading: "메뉴를 읽는 중…",
  done: "판정 화면으로 이동하는 중…",
} as const;

export function ScanFlow() {
  const { phase, busy, message, emptyInput, scan, submitManual, closeEmptyInput } = useScan();

  return (
    <div className="flex flex-col gap-6">
      <PhotoPicker disabled={busy} onPick={scan} />

      {phase in PROGRESS && (
        <p role="status" className="text-center text-neutral-600">
          {PROGRESS[phase as keyof typeof PROGRESS]}
        </p>
      )}

      {message && (
        <p role="alert" className={`text-sm ${phase === "manual" ? "text-neutral-700" : "text-red-600"}`}>
          {message}
        </p>
      )}

      {phase === "manual" && <ManualMenuInput disabled={busy} onSubmit={submitManual} />}

      {/* 결과 화면(①)의 빈 입력 팝업과 같은 모양·문구다(problem.yaml > features > REQ-3). */}
      {emptyInput && (
        <div
          role="alertdialog"
          aria-modal="true"
          aria-labelledby="scan-empty-input"
          className="fixed inset-0 z-10 flex items-center justify-center bg-black/40 px-6"
        >
          <div className="w-full max-w-xs rounded-xl bg-white p-5 shadow-lg">
            <p id="scan-empty-input" className="mb-4 text-center font-semibold">
              입력 값이 없습니다.
            </p>
            <button
              type="button"
              autoFocus
              onClick={closeEmptyInput}
              className="w-full rounded-lg bg-emerald-600 px-4 py-2 font-semibold text-white"
            >
              확인
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
