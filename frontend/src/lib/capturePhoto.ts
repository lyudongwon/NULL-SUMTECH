// REQ-1 카메라 촬영: <video>의 현재 화면을 JPEG 파일로 만든다. 줄이는 일은 업로드 직전에 resizeImage(OPT-3)가 한다.

export const CAPTURE_QUALITY = 0.92;

/** 영상 한 장면을 JPEG 파일로 만든다. 영상이 아직 시작되지 않았거나 만들지 못하면 null. */
export async function capturePhoto(video: HTMLVideoElement): Promise<File | null> {
  const width = video.videoWidth;
  const height = video.videoHeight;
  if (!width || !height) return null;

  const canvas = document.createElement("canvas");
  canvas.width = width;
  canvas.height = height;
  const context = canvas.getContext("2d");
  if (!context) return null;
  context.drawImage(video, 0, 0, width, height);

  const blob = await new Promise<Blob | null>((resolve) => canvas.toBlob(resolve, "image/jpeg", CAPTURE_QUALITY));
  if (!blob) return null;
  return new File([blob], `camera-${Date.now()}.jpg`, { type: "image/jpeg" });
}
