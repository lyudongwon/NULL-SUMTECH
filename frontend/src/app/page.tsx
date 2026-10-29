import Link from "next/link";

// 첫 화면: 로그인 없이 서비스 이름과 하는 일이 보인다(progress.md 5단계).
// TODO(확인): 서비스 이름은 팀이 정하지 않아 임시로 "학식 식단 체커"를 쓴다.
export default function Home() {
  return (
    <section className="flex flex-col gap-8 pt-6">
      <div className="flex flex-col gap-3">
        <h1 className="text-3xl font-bold">학식 식단 체커</h1>
        <p className="text-neutral-700">
          학식 메뉴판이나 음식 사진 한 장으로, 내 식단 제한(알레르기·할랄·채식)에 맞는지{" "}
          <strong>안전 / 확인 필요 / 불가</strong>로 알려 드려요.
        </p>
      </div>

      <nav className="flex flex-col gap-3">
        <Link
          href="/scan"
          className="rounded-lg bg-emerald-600 px-4 py-3 text-center font-semibold text-white"
        >
          사진 올리기
        </Link>
        <Link
          href="/settings"
          className="rounded-lg border border-neutral-300 px-4 py-3 text-center font-semibold"
        >
          식단 제한 설정
        </Link>
        <Link
          href="/restaurants"
          className="rounded-lg border border-neutral-300 px-4 py-3 text-center font-semibold"
        >
          다른 음식점 추천
        </Link>
      </nav>
    </section>
  );
}
