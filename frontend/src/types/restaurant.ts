// OPT-2 음식점 추천 (docs/api.md). 역할 ①이 정한다.

export type Restaurant = {
  restaurant_id: number;
  name: string;
  category: string | null;
  address: string | null;
  matched_restrictions: string[];
};

export type RecommendationResponse = { restaurants: Restaurant[] };

// OPT-5 음식점 성분 제보 (docs/api.md)
export type FeedbackRequest = { item_code: string; menu_name?: string; note?: string };

export type FeedbackResponse = {
  restaurant_id: number;
  item_code: string;
  menu_name: string | null;
  note: string | null;
  report_count: number;
};
