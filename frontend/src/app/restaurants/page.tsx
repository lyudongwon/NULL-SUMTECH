import Link from "next/link";
import { RestaurantRecommendations } from "@/components/restaurants/RestaurantRecommendations";

export const metadata = { title: "Other restaurants | Can I Eat?" };

export default function RestaurantsPage() {
  return (
    <section className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">Other restaurants</h1>
        <Link href="/" className="text-sm text-neutral-500">
          Home
        </Link>
      </div>
      <p className="text-sm text-neutral-600">Restaurants that fit your diet, for when the cafeteria doesn&apos;t.</p>
      <RestaurantRecommendations />
    </section>
  );
}
