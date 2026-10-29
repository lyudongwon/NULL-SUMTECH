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
  const { phase, busy, message, scan, submitManual } = useScan();

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
    </div>
  );
}
