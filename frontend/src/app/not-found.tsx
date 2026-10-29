import Link from "next/link";
import { PageHead } from "@/components/layout/PageHead";
import { btnOutline, btnPrimary, card } from "@/components/ui/styles";

export const metadata = { title: "Page not found | Can I Eat?" };

// 없는 주소. Next가 404 상태로 이 화면을 보여 준다. 다른 화면과 같은 제목줄·카드·버튼을 쓴다.
export default function NotFound() {
  return (
    <div className="flex flex-col gap-6">
      <PageHead title="Page not found" lead="This page doesn't exist or has moved." />
      <div className={`${card} flex flex-wrap gap-2 p-6`}>
        <Link href="/" className={btnPrimary}>
          Go home
        </Link>
        <Link href="/scan" className={btnOutline}>
          Upload photo
        </Link>
      </div>
    </div>
  );
}
