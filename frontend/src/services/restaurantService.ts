import { apiRequest } from "@/lib/apiClient";
import type { FeedbackRequest, FeedbackResponse, RecommendationResponse } from "@/types/restaurant";

export function getRecommendations(userId: number, limit = 5) {
  return apiRequest<RecommendationResponse>(
    `/restaurants/recommendations?user_id=${userId}&limit=${limit}`,
  );
}

export function submitFeedback(restaurantId: number, body: FeedbackRequest) {
  return apiRequest<FeedbackResponse>(`/restaurants/${restaurantId}/feedback`, {
    method: "POST",
    body: JSON.stringify(body),
  });
}
