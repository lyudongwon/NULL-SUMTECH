import { apiRequest } from "@/lib/apiClient";
import type { MenuRecognizeRequest, MenuRecognizeResponse } from "@/types/menu";

export function recognizeMenus(imageId: string) {
  const request: MenuRecognizeRequest = { image_id: imageId };
  return apiRequest<MenuRecognizeResponse>("/menus/recognize", {
    method: "POST",
    body: JSON.stringify(request),
  });
}
