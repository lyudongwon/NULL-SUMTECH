// OPT-3 이미지 축소 (docs/api.md). 업로드 직전에 브라우저에서 긴 변 1400px, JPEG 품질 0.85로 줄인다.

export const MAX_LONG_SIDE = 1400;
export const JPEG_QUALITY = 0.85;

/** 줄일 크기. 긴 변이 1400px 미만이면 null(줄이지 않고 원본을 보낸다). */
export function targetSize(width: number, height: number): { width: number; height: number } | null {
  const longSide = Math.max(width, height);
  if (longSide < MAX_LONG_SIDE) return null;
  const scale = MAX_LONG_SIDE / longSide;
  return { width: Math.max(1, Math.round(width * scale)), height: Math.max(1, Math.round(height * scale)) };
}

/**
 * 긴 변이 1400px 이상이면 긴 변 1400px JPEG로 줄인 새 파일을, 아니면 원본을 돌려준다.
 * 브라우저가 열지 못하는 형식이면 원본을 그대로 돌려주고 형식 판단은 서버(415)에 맡긴다.
 */
export async function resizeImage(file: File): Promise<File> {
  let bitmap: ImageBitmap;
  try {
    // 휴대폰 사진의 회전 정보(EXIF)를 반영한 크기로 연다.
    bitmap = await createImageBitmap(file, { imageOrientation: "from-image" });
  } catch {
    return file;
  }
  try {
    const size = targetSize(bitmap.width, bitmap.height);
    if (!size) return file;

    const canvas = document.createElement("canvas");
    canvas.width = size.width;
    canvas.height = size.height;
    const context = canvas.getContext("2d");
    if (!context) return file;
    // 투명한 PNG가 JPEG에서 검게 나오지 않도록 흰 바탕을 깐다.
    context.fillStyle = "#ffffff";
    context.fillRect(0, 0, size.width, size.height);
    context.drawImage(bitmap, 0, 0, size.width, size.height);

    const blob = await new Promise<Blob | null>((resolve) => canvas.toBlob(resolve, "image/jpeg", JPEG_QUALITY));
    if (!blob) return file;
    return new File([blob], file.name.replace(/\.[^.]*$/, "") + ".jpg", { type: "image/jpeg" });
  } finally {
    bitmap.close();
  }
}
