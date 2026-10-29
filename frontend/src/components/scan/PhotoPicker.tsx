"use client";

import { useRef, useState, type ChangeEvent, type DragEvent } from "react";
import { Icon } from "@/components/ui/icons";
import { btnOutline, btnPrimary } from "@/components/ui/styles";
import { cameraSupported } from "@/hooks/useCamera";
import type { ImageSource } from "@/types/image";
import { CameraCapture } from "./CameraCapture";

type Props = { disabled: boolean; onPick: (file: File, source: ImageSource) => void };

const ACCEPT = "image/jpeg,image/png,image/webp";

/**
 * REQ-1 사진 고르기: 카메라 촬영, 앨범·파일 선택, 드래그 앤 드롭.
 * 촬영은 화면 안 카메라(CameraCapture)를 먼저 열고, 쓸 수 없으면(허용 거부·카메라 없음·http 접속) 기기 기본 카메라로 넘어간다.
 * 파일 입력은 하나만 두고 버튼에 따라 capture를 붙였다 뗀다(E2E가 input[type="file"] 하나를 찾는다).
 */
export function PhotoPicker({ disabled, onPick }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const sourceRef = useRef<ImageSource>("FILE");
  const [dragging, setDragging] = useState(false);
  const [cameraOpen, setCameraOpen] = useState(false);

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

  function takePhoto() {
    if (disabled) return;
    if (cameraSupported()) setCameraOpen(true);
    else open("CAMERA");
  }

  function closeCamera(reason?: "unavailable") {
    setCameraOpen(false);
    if (reason === "unavailable") open("CAMERA");
  }

  function onChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    const source = sourceRef.current;
    // 같은 사진을 다시 골라도 change가 일어나도록 비우고, 다음 선택은 기본(FILE)으로 돌린다.
    event.target.value = "";
    sourceRef.current = "FILE";
    if (file) onPick(file, source);
  }

  function onDrop(event: DragEvent<HTMLElement>) {
    event.preventDefault();
    setDragging(false);
    const file = event.dataTransfer.files?.[0];
    if (file && !disabled) onPick(file, "DRAG_DROP");
  }

  return (
    <section
      data-testid="scan-drop-zone"
      onDragOver={(event) => {
        event.preventDefault();
        if (!disabled) setDragging(true);
      }}
      onDragLeave={() => setDragging(false)}
      onDrop={onDrop}
      className={`flex flex-col items-center justify-center gap-[18px] rounded-3xl border-[1.5px] border-dashed px-5 pt-10 pb-8 text-center transition-colors md:px-8 md:pt-12 ${
        dragging ? "border-olive bg-[#f7f9ef]" : "border-[#c3c8d0] bg-surface"
      }`}
    >
      <div className="flex h-16 w-16 items-center justify-center rounded-[18px] bg-chip text-olive">
        <Icon name="upload" size={30} stroke={1.7} />
      </div>
      <div className="flex flex-col gap-1.5">
        <p className="text-[19px] font-semibold">Upload one photo of a menu board or a dish.</p>
        <p className="hidden text-sm text-muted sm:block">You can also drag a photo here.</p>
      </div>
      <div className="flex w-full flex-wrap justify-center gap-2.5">
        <button type="button" disabled={disabled} onClick={takePhoto} className={`${btnPrimary} flex-1 sm:flex-none`}>
          <Icon name="camera" />
          Take a photo
        </button>
        <button type="button" disabled={disabled} onClick={() => open("FILE")} className={`${btnOutline} flex-1 sm:flex-none`}>
          <Icon name="image" />
          Choose from album
        </button>
      </div>
      {cameraOpen && (
        <CameraCapture
          onUse={(file) => {
            setCameraOpen(false);
            onPick(file, "CAMERA");
          }}
          onClose={closeCamera}
        />
      )}
      <input
        ref={inputRef}
        type="file"
        accept={ACCEPT}
        aria-label="Photo file"
        data-testid="scan-file-input"
        className="sr-only"
        tabIndex={-1}
        disabled={disabled}
        onChange={onChange}
      />
    </section>
  );
}
