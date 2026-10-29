"use client";

import { useState, type ChangeEvent, type DragEvent } from "react";
import type { ImageSource } from "@/types/image";

type Props = { disabled: boolean; onPick: (file: File, source: ImageSource) => void };

const ACCEPT = "image/jpeg,image/png,image/webp";

/** REQ-1 사진 고르기: 카메라 촬영, 앨범·파일 선택, 드래그 앤 드롭. */
export function PhotoPicker({ disabled, onPick }: Props) {
  const [dragging, setDragging] = useState(false);

  function onChange(source: ImageSource) {
    return (event: ChangeEvent<HTMLInputElement>) => {
      const file = event.target.files?.[0];
      // 같은 사진을 다시 골라도 change가 일어나도록 비운다.
      event.target.value = "";
      if (file) onPick(file, source);
    };
  }

  function onDrop(event: DragEvent<HTMLDivElement>) {
    event.preventDefault();
    setDragging(false);
    const file = event.dataTransfer.files?.[0];
    if (file && !disabled) onPick(file, "DRAG_DROP");
  }

  const button =
    "flex-1 cursor-pointer rounded-lg px-4 py-3 text-center font-semibold has-[:disabled]:cursor-not-allowed has-[:disabled]:opacity-60";

  return (
    <div
      data-testid="scan-drop-zone"
      onDragOver={(event) => {
        event.preventDefault();
        if (!disabled) setDragging(true);
      }}
      onDragLeave={() => setDragging(false)}
      onDrop={onDrop}
      className={`flex flex-col gap-4 rounded-xl border-2 border-dashed p-5 ${
        dragging ? "border-emerald-500 bg-emerald-50" : "border-neutral-300"
      }`}
    >
      <p className="text-center text-sm text-neutral-600">
        메뉴판이나 음식 사진 한 장을 올려 주세요.
        <span className="hidden sm:inline"> 사진을 여기로 끌어다 놓아도 돼요.</span>
      </p>
      <div className="flex gap-3">
        <label className={`${button} bg-emerald-600 text-white`}>
          카메라로 찍기
          <input
            type="file"
            accept="image/*"
            capture="environment"
            className="sr-only"
            disabled={disabled}
            onChange={onChange("CAMERA")}
          />
        </label>
        <label className={`${button} border border-neutral-300`}>
          앨범에서 고르기
          <input
            type="file"
            accept={ACCEPT}
            className="sr-only"
            data-testid="scan-file-input"
            disabled={disabled}
            onChange={onChange("FILE")}
          />
        </label>
      </div>
    </div>
  );
}
