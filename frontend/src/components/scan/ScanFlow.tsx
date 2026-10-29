"use client";

import { useEffect, useState } from "react";
import { Icon } from "@/components/ui/icons";
import { btnOlive, card } from "@/components/ui/styles";
import { useScan } from "@/hooks/useScan";
import type { ImageSource } from "@/types/image";
import { ManualMenuInput } from "./ManualMenuInput";
import { PhotoPicker } from "./PhotoPicker";

// 진행 단계 문구는 docs/api.md > 화면 문구(사진 올리기 화면 진행 상태)를 따른다.
const STEPS = ["Uploading photo…", "Reading the menu…", "Opening results…"];
const STEP_OF = { uploading: 0, reading: 1, done: 2 } as const;

export function ScanFlow() {
  const { phase, busy, message, emptyInput, scan, submitManual, closeEmptyInput } = useScan();
  const [preview, setPreview] = useState<string | null>(null);

  // 미리보기 주소는 다른 사진을 고르거나 화면을 떠날 때 해제한다.
  useEffect(() => () => {
    if (preview) URL.revokeObjectURL(preview);
  }, [preview]);

  function pick(file: File, source: ImageSource) {
    setPreview(URL.createObjectURL(file));
    scan(file, source);
  }

  if (busy) {
    const step = STEP_OF[phase as keyof typeof STEP_OF];
    return (
      <div className="flex flex-col gap-4 md:flex-row md:gap-8">
        <PhotoPreview url={preview} scanning />
        <ol aria-live="polite" className={`${card} self-start md:min-w-[320px] md:flex-1`}>
          {STEPS.map((label, i) => {
            const state = i < step ? "done" : i === step ? "active" : "todo";
            return (
              <li
                key={label}
                role={state === "active" ? "status" : undefined}
                className={`flex items-center gap-3 border-b border-divider px-[18px] py-[15px] text-[15px] last:border-b-0 ${
                  state === "active" ? "font-semibold text-olive-dark" : state === "todo" ? "text-muted" : ""
                }`}
              >
                <StepMark state={state} />
                {label}
              </li>
            );
          })}
        </ol>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-4">
      {message && phase !== "manual" && (
        <p role="alert" className="rounded-[14px] bg-avoid-bg px-4 py-3 text-sm font-medium text-avoid">
          {message}
        </p>
      )}

      {phase === "manual" && (
        <section className={`${card} flex flex-col gap-4 p-5 md:p-6`}>
          {message && (
            <p role="alert" className="flex items-start gap-2 text-[15px] text-body">
              <span className="mt-0.5 text-check">
                <Icon name="bang" stroke={2.4} />
              </span>
              {message}
            </p>
          )}
          <ManualMenuInput disabled={busy} onSubmit={submitManual} />
        </section>
      )}

      <PhotoPicker disabled={busy} onPick={pick} />

      {/* 결과 화면의 빈 입력 팝업과 같은 모양·문구다(problem.yaml > features > REQ-3). */}
      {emptyInput && <EmptyInputDialog onClose={closeEmptyInput} />}
    </div>
  );
}

function PhotoPreview({ url, scanning }: { url: string | null; scanning: boolean }) {
  return (
    <div className="relative flex min-h-[260px] flex-1 items-center justify-center overflow-hidden rounded-[20px] bg-[repeating-linear-gradient(135deg,#e6e9ed_0_12px,#dde1e6_12px_24px)] md:min-h-[420px]">
      {url && (
        // eslint-disable-next-line @next/next/no-img-element -- 방금 고른 사진의 임시 주소(blob:)라 next/image를 쓸 수 없다
        <img src={url} alt="Uploaded photo" className="absolute inset-0 h-full w-full object-cover" />
      )}
      {scanning && (
        <div
          className="absolute right-0 left-0 h-[3px] bg-olive shadow-[0_0_22px_5px_rgba(101,113,4,0.35)] motion-safe:animate-[scan-line_2.2s_ease-in-out_infinite_alternate]"
          style={{ top: "8%" }}
        />
      )}
    </div>
  );
}

function StepMark({ state }: { state: "done" | "active" | "todo" }) {
  if (state === "done")
    return (
      <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-ink text-white">
        <Icon name="check" size={14} stroke={2.6} />
      </span>
    );
  if (state === "active")
    return <span className="h-6 w-6 shrink-0 animate-spin rounded-full border-[2.5px] border-edge border-t-olive" />;
  return <span className="h-6 w-6 shrink-0 rounded-full border-[1.5px] border-[#cdd2d9]" />;
}

export function EmptyInputDialog({ onClose }: { onClose: () => void }) {
  return (
    <div className="fixed inset-0 z-10 flex items-center justify-center bg-[rgba(40,54,81,0.55)] p-6">
      <div
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="empty-input-title"
        className="flex w-[360px] max-w-full flex-col items-center gap-2 rounded-[20px] bg-surface px-6 pt-7 pb-5 text-center"
      >
        <span className="flex h-12 w-12 items-center justify-center rounded-full bg-check-bg text-check">
          <Icon name="bang" size={22} stroke={2.4} />
        </span>
        <p id="empty-input-title" className="text-lg font-bold">
          Please enter a menu name.
        </p>
        <button type="button" autoFocus onClick={onClose} className={`${btnOlive} mt-3 w-full`}>
          OK
        </button>
      </div>
    </div>
  );
}
