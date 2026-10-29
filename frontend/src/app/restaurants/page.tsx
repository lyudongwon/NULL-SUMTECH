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
    </div>
  );
}
