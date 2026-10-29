import { apiRequest } from "@/lib/apiClient";
import type { FeedbackRequest, FeedbackResponse, RecommendationResponse } from "@/types/restaurant";

export function getRecommendations(userId: number, limit = 5) {
  return apiRequest<RecommendationResponse>(`/restaurants/recommendations?user_id=${userId}&limit=${limit}`);
}

export function submitFeedback(restaurantId: number, body: FeedbackRequest) {
  return apiRequest<FeedbackResponse>(`/restaurants/${restaurantId}/feedback`, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

/** 내가 보낸 제보를 취소한다. 없으면 아무 일도 하지 않는다(204). */
export function cancelFeedback(restaurantId: number, itemCode: string) {
  return apiRequest<null>(`/restaurants/${restaurantId}/feedback/${encodeURIComponent(itemCode)}`, {
    method: "DELETE",
  });
}
