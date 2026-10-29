import { ApiError } from "@/lib/apiClient";
import type { ImageSource, ImageUploadResponse } from "@/types/image";

// apiRequest는 Content-Type을 JSON으로 고정해 multipart를 보낼 수 없어서 여기서만 fetch를 직접 쓴다.
// apiRequest가 FormData 본문을 받게 되면 apiRequest로 바꿀 수 있다.
export async function uploadImage(file: File, source: ImageSource): Promise<ImageUploadResponse> {
  const form = new FormData();
  form.append("file", file, file.name);
  form.append("source", source);

  let response: Response;
  try {
    response = await fetch("/api/v1/images", { method: "POST", body: form, cache: "no-store" });
  } catch {
    throw new ApiError(0, "Can't reach the server. Please try again shortly.");
  }
  const body = await response.json().catch(() => null);
  if (!response.ok) {
    throw new ApiError(response.status, body?.detail ?? "Couldn't upload the photo.");
  }
  return body as ImageUploadResponse;
}
