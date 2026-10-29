"use client";

import { useCallback, useEffect, useRef, useState } from "react";

/** idle: 꺼짐, starting: 허용을 기다리는 중, live: 미리보기 중, failed: 열지 못함(허용 거부·카메라 없음·http 접속 등) */
export type CameraState = "idle" | "starting" | "live" | "failed";

/** 브라우저가 화면 안 카메라(getUserMedia)를 쓸 수 있는지. http 주소에서는 localhost만 가능하다. */
export function cameraSupported() {
  return typeof navigator !== "undefined" && !!navigator.mediaDevices?.getUserMedia;
}

/** REQ-1 화면 안 카메라. 뒷면 카메라를 먼저 요청하고, 닫거나 화면을 벗어나면 반드시 끈다. */
export function useCamera() {
  const videoRef = useRef<HTMLVideoElement>(null);
  const streamRef = useRef<MediaStream | null>(null);
  // start()·stop()을 부를 때마다 올린다. 늦게 끝난 옛 start()가 새 상태를 덮어쓰지 못하게 한다(개발 모드는 effect를 두 번 돈다).
  const runRef = useRef(0);
  const [state, setState] = useState<CameraState>("idle");

  const stop = useCallback(() => {
    runRef.current += 1;
    streamRef.current?.getTracks().forEach((track) => track.stop());
    streamRef.current = null;
    if (videoRef.current) videoRef.current.srcObject = null;
    setState("idle");
  }, []);

  const start = useCallback(async () => {
    if (!cameraSupported()) {
      setState("failed");
      return false;
    }
    const run = ++runRef.current;
    setState("starting");
    let stream: MediaStream | null = null;
    try {
      stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: { ideal: "environment" } },
        audio: false,
      });
      const video = videoRef.current;
      if (run !== runRef.current || !video) {
        // 허용을 기다리는 사이 닫혔거나 다시 시작됐다. 이 스트림만 끄고 상태는 건드리지 않는다.
        stream.getTracks().forEach((track) => track.stop());
        return false;
      }
      streamRef.current = stream;
      video.srcObject = stream;
      await video.play();
      if (run !== runRef.current) return false;
      setState("live");
      return true;
    } catch {
      if (run !== runRef.current) return false;
      stream?.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
      setState("failed");
      return false;
    }
  }, []);

  useEffect(() => stop, [stop]);

  return { videoRef, state, start, stop };
}
