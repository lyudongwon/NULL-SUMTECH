"use client";

import { useRef, useState, type ChangeEvent, type DragEvent } from "react";
import type { ImageSource } from "@/types/image";

type Props = { disabled: boolean; onPick: (file: File, source: ImageSource) => void };

const ACCEPT = "image/jpeg,image/png,image/webp";

/**
 * REQ-1 사진 고르기: 카메라 촬영, 앨범·파일 선택, 드래그 앤 드롭.
 * 파일 입력은 하나만 두고 버튼에 따라 capture를 붙였다 뗀다(E2E가 input[type="file"] 하나를 찾는다).
 */
export function PhotoPicker({ disabled, onPick }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const sourceRef = useRef<ImageSource>("FILE");
  const [dragging, setDragging] = useState(false);

  function open(source: "FILE" | "CAMERA") {
    const input = inputRef.current;
    if (!input || disabled) return;
    sourceRef.current = source;
    if (source === "CAMERA") {
      input.accept = "image/*";
      input.setAttribute("capture", "environment");
    } else {
      input.accept = ACCEPT;
      input.removeAttribute("capture");
    }
    input.click();
  }

  function onChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    const source = sourceRef.current;
    // 같은 사진을 다시 골라도 change가 일어나도록 비우고, 다음 선택은 기본(FILE)으로 돌린다.
    event.target.value = "";
    sourceRef.current = "FILE";
    if (file) onPick(file, source);
  }

  function onDrop(event: DragEvent<HTMLDivElement>) {
    event.preventDefault();
    setDragging(false);
    const file = event.dataTransfer.files?.[0];
    if (file && !disabled) onPick(file, "DRAG_DROP");
  }

  const button = "flex-1 rounded-lg px-4 py-3 text-center font-semibold disabled:opacity-60";

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
        <button type="button" disabled={disabled} onClick={() => open("CAMERA")} className={`${button} bg-emerald-600 text-white`}>
          카메라로 찍기
        </button>
        <button type="button" disabled={disabled} onClick={() => open("FILE")} className={`${button} border border-neutral-300`}>
          앨범에서 고르기
        </button>
      </div>
      <input
        ref={inputRef}
        type="file"
        accept={ACCEPT}
        aria-label="사진 파일"
        data-testid="scan-file-input"
        className="sr-only"
        tabIndex={-1}
        disabled={disabled}
        onChange={onChange}
      />
    </div>
  );
}
