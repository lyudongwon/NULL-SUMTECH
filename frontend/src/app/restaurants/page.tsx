import { PageHead } from "@/components/layout/PageHead";
import { RestaurantRecommendations } from "@/components/restaurants/RestaurantRecommendations";

export const metadata = { title: "Other restaurants | Can I Eat?" };

export default function RestaurantsPage() {
  return (
    <div className="flex flex-1 flex-col gap-6">
      <PageHead
        title="Other restaurants"
        lead="Restaurants that fit your diet, for when the cafeteria doesn't."
      />
      <RestaurantRecommendations />
      {/* 음식점 시드가 아직 예시 데이터다(R__20_seed_restaurants.sql). 조사 데이터로 바꾸면 이 줄을 지운다. */}
      <p className="text-[13px] text-muted">Sample list for now. We&apos;re still adding real restaurants near campus.</p>
    </div>
  );
}
