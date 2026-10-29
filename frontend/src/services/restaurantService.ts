import { apiRequest } from "@/lib/apiClient";
import type { RecommendationResponse } from "@/types/restaurant";

export function getRecommendations(userId: number, limit = 5) {
  return apiRequest<RecommendationResponse>(
    `/restaurants/recommendations?user_id=${userId}&limit=${limit}`,
  );
}
