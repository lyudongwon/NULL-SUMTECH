import Link from "next/link";
import { RestaurantRecommendations } from "@/components/restaurants/RestaurantRecommendations";

export const metadata = { title: "다른 음식점 추천 | 학식 식단 체커" };

export default function RestaurantsPage() {
  return (
    <section className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">다른 음식점 추천</h1>
        <Link href="/" className="text-sm text-neutral-500">
          처음으로
        </Link>
      </div>
      <p className="text-sm text-neutral-600">학식을 먹기 어려울 때, 내 식단 제한에 맞는 음식점이에요.</p>
      <RestaurantRecommendations />
    </section>
  );
}
