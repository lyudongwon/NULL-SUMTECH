import Link from "next/link";
import { RestrictionSettings } from "@/components/settings/RestrictionSettings";

export const metadata = { title: "식단 제한 설정 | 학식 식단 체커" };

export default function SettingsPage() {
  return (
    <section className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">식단 제한 설정</h1>
        <Link href="/" className="text-sm text-neutral-500">
          처음으로
        </Link>
      </div>
      <p className="text-sm text-neutral-600">
        프리셋을 고르거나 못 먹는 음식을 하나씩 켜고 끈 뒤 저장하세요.
      </p>
      <RestrictionSettings />
    </section>
  );
}
