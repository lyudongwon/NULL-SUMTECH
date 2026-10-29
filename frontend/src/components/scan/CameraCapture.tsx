"use client";

import { useEffect, useRef, useState } from "react";
import { useCamera } from "@/hooks/useCamera";
import { capturePhoto } from "@/lib/capturePhoto";

type Props = {
  /** 촬영한 사진을 쓰기로 했을 때 */
  onUse: (file: File) => void;
  /** 닫기, 또는 카메라를 열 수 없을 때(이때 reason이 있다) */
  onClose: (reason?: "unavailable") => void;
};

/**
 * REQ-1 화면 안 카메라: 미리보기 → "Take photo" → 찍은 사진 확인 → "Use photo" 또는 "Retake".
 * 열지 못하면 onClose("unavailable")로 알려 PhotoPicker가 기기 기본 카메라(capture 입력)로 넘어가게 한다.
 */
export function CameraCapture({ onUse, onClose }: Props) {
  const { videoRef, state, start, stop } = useCamera();
  const [shot, setShot] = useState<{ file: File; url: string } | null>(null);
  const shotUrlRef = useRef<string | null>(null);

  useEffect(() => {
    void start();
  }, [start]);

  useEffect(() => {
    if (state === "failed") onClose("unavailable");
  }, [state, onClose]);

  // 미리보기 주소는 바꾸거나 닫을 때 해제한다.
  useEffect(() => {
    shotUrlRef.current = shot?.url ?? null;
  }, [shot]);
  useEffect(() => () => {
    if (shotUrlRef.current) URL.revokeObjectURL(shotUrlRef.current);
  }, []);

  async function take() {
    const video = videoRef.current;
    if (!video) return;
    const file = await capturePhoto(video);
    if (!file) return;
    if (shot) URL.revokeObjectURL(shot.url);
    setShot({ file, url: URL.createObjectURL(file) });
  }

  function retake() {
    if (shot) URL.revokeObjectURL(shot.url);
    setShot(null);
  }

  function close() {
    stop();
    onClose();
  }

  function use() {
    if (!shot) return;
    stop();
    onUse(shot.file);
  }

  const button = "flex-1 rounded-lg px-4 py-3 font-semibold disabled:opacity-60";

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="Camera"
      data-testid="camera-capture"
      className="fixed inset-0 z-20 flex flex-col bg-black"
    >
      <div className="relative min-h-0 flex-1">
        <video
          ref={videoRef}
          playsInline
          muted
          aria-label="Camera preview"
          className={`h-full w-full object-contain ${shot ? "hidden" : ""}`}
        />
        {shot && (
          // eslint-disable-next-line @next/next/no-img-element -- 방금 찍은 사진의 임시 주소(blob:)라 next/image를 쓸 수 없다
          <img src={shot.url} alt="Photo you just took" className="h-full w-full object-contain" />
        )}
        {state === "starting" && (
          <p role="status" className="absolute inset-0 flex items-center justify-center text-white">
            Opening camera…
          </p>
        )}
      </div>
      <div className="flex gap-3 bg-black p-4 pb-[max(1rem,env(safe-area-inset-bottom))]">
        {shot ? (
          <>
            <button type="button" onClick={retake} className={`${button} border border-white/40 text-white`}>
              Retake
            </button>
            <button type="button" onClick={use} className={`${button} bg-emerald-600 text-white`}>
              Use photo
            </button>
          </>
        ) : (
          <>
            <button type="button" onClick={close} className={`${button} border border-white/40 text-white`}>
              Cancel
            </button>
            <button
              type="button"
              disabled={state !== "live"}
              onClick={take}
              className={`${button} bg-emerald-600 text-white`}
            >
              Take photo
            </button>
          </>
        )}
      </div>
    </div>
  );
}
