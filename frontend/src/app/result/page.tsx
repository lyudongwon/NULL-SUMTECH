import { Suspense } from "react";
import { JudgmentResults } from "@/components/result/JudgmentResults";

export const metadata = { title: "Results | Can I Eat?" };

// /result?menus=<JSON 배열> — ②의 /scan이 판독한 메뉴 이름을 넘긴다(docs/roles.md 만나는 지점 1).
export default function ResultPage() {
  return (
    <Suspense fallback={<p className="text-body">Checking…</p>}>
      <JudgmentResults />
    </Suspense>
  );
}
