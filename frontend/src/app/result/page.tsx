import Link from "next/link";
import { Suspense } from "react";
import { JudgmentResults } from "@/components/result/JudgmentResults";

export const metadata = { title: "판정 결과 | 학식 식단 체커" };

// /result?menus=<JSON 배열> — ②의 /scan이 판독한 메뉴 이름을 넘긴다(docs/roles.md 만나는 지점 1).
export default function ResultPage() {
  return (
    <section className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">판정 결과</h1>
        <div className="flex gap-3 text-sm text-neutral-500">
          <Link href="/scan">다시 찍기</Link>
          <Link href="/">처음으로</Link>
        </div>
      </div>
      <Suspense fallback={<p className="text-neutral-500">판정하는 중…</p>}>
        <JudgmentResults />
      </Suspense>
    </section>
  );
}
