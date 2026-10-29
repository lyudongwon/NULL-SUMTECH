// OPT-2 음식점 추천 (docs/api.md). 역할 ①이 정한다.

export type Restaurant = {
  restaurant_id: number;
  name: string;
  category: string | null;
  address: string | null;
  matched_restrictions: string[];
};

export type RecommendationResponse = { restaurants: Restaurant[] };
