import { apiRequest } from "@/lib/apiClient";
import type {
  PresetListResponse,
  RestrictionPatchRequest,
  RestrictionResponse,
} from "@/types/restriction";

export function getPresets() {
  return apiRequest<PresetListResponse>("/restrictions/presets");
}

export function getRestrictions(userId: number) {
  return apiRequest<RestrictionResponse>(`/users/${userId}/restrictions`);
}

export function saveRestrictions(userId: number, request: RestrictionPatchRequest) {
  return apiRequest<RestrictionResponse>(`/users/${userId}/restrictions`, {
    method: "PATCH",
    body: JSON.stringify(request),
  });
}
