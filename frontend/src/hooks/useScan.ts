"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { ApiError } from "@/lib/apiClient";
import { resizeImage } from "@/lib/resizeImage";
import { uploadImage } from "@/services/imageService";
import { recognizeMenus } from "@/services/menuService";
import type { ImageSource } from "@/types/image";
import type { RecognizedMenu } from "@/types/menu";

/** idle: 사진 고르기 전·업로드 실패, manual: 판독 실패로 메뉴 이름 입력칸(OPT-1)을 보여 줌 */
export type ScanPhase = "idle" | "uploading" | "reading" | "manual" | "done";

/** 읽은 이름만 순서대로 모은다(null·빈 이름·중복 제외). */
export function menuNames(menus: RecognizedMenu[]): string[] {
  const names = menus.map((m) => m.name?.trim() ?? "").filter((name) => name !== "");
  return [...new Set(names)];
}

/** 결과 화면(①) 주소. docs/roles.md > 만나는 지점 1 */
export function resultUrl(names: string[]) {
  return `/result?menus=${encodeURIComponent(JSON.stringify(names))}`;
}

/** REQ-1 업로드(OPT-3 축소) → REQ-2 판독 → /result 이동. 판독에 실패하면 OPT-1 입력칸으로 넘어간다. */
export function useScan() {
  const router = useRouter();
  const [phase, setPhase] = useState<ScanPhase>("idle");
  const [message, setMessage] = useState<string | null>(null);
  const busy = phase === "uploading" || phase === "reading" || phase === "done";

  async function scan(file: File, source: ImageSource) {
    if (busy) return;
    setMessage(null);

    setPhase("uploading");
    let imageId: string;
    try {
      imageId = (await uploadImage(await resizeImage(file), source)).image_id;
    } catch (error) {
      setMessage(error instanceof ApiError ? error.detail : "사진을 올리지 못했습니다.");
      setPhase("idle");
      return;
    }

    setPhase("reading");
    try {
      const names = menuNames((await recognizeMenus(imageId)).menus);
      if (names.length === 0) {
        setMessage("사진에서 메뉴를 읽지 못했습니다. 메뉴 이름을 직접 입력해 주세요.");
        setPhase("manual");
        return;
      }
      setPhase("done");
      router.push(resultUrl(names));
    } catch (error) {
      // 422(메뉴판·음식 사진이 아님)와 500(AI 오류·시간 초과)은 입력칸을 보여 준다(docs/api.md > REQ-2).
      if (error instanceof ApiError && (error.status === 422 || error.status >= 500)) {
        setMessage(error.detail);
        setPhase("manual");
        return;
      }
      setMessage(error instanceof ApiError ? error.detail : "메뉴를 읽지 못했습니다.");
      setPhase("idle");
    }
  }

  /** OPT-1 "판정하기". 이름이 비었으면 결과 화면과 같은 문구로 알리고 넘어가지 않는다. */
  function submitManual(name: string) {
    const trimmed = name.trim();
    if (!trimmed) {
      window.alert("입력 값이 없습니다.");
      return;
    }
    setPhase("done");
    router.push(resultUrl([trimmed]));
  }

  return { phase, busy, message, scan, submitManual };
}
