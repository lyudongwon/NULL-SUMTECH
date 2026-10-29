import Link from "next/link";
import { ScanFlow } from "@/components/scan/ScanFlow";

export const metadata = { title: "사진 올리기 | 학식 식단 체커" };

export default function ScanPage() {
  return (
    <section className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">사진 올리기</h1>
        <Link href="/" className="text-sm text-neutral-500">
          처음으로
        </Link>
      </div>
      <ScanFlow />
    </section>
  );
}
