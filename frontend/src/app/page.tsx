import Link from "next/link";

// 첫 화면: 로그인 없이 서비스 이름과 하는 일이 보인다(progress.md 5단계).
// TODO(확인): 서비스 이름은 팀이 정하지 않아 임시로 "학식 식단 체커"를 쓴다.
export default function Home() {
  return (
    <section className="flex flex-col gap-8 pt-6">
      <div className="flex flex-col gap-3">
        <h1 className="text-3xl font-bold">Can I Eat?</h1>
        <p className="text-neutral-700">
          Snap a cafeteria menu or dish, and see if it fits your diet:{" "}
          <strong>Safe, Check first, or Avoid</strong>.
        </p>
      </div>

      <nav className="flex flex-col gap-3">
        <Link
          href="/scan"
          className="rounded-lg bg-emerald-600 px-4 py-3 text-center font-semibold text-white"
        >
          Upload photo
        </Link>
        <Link
          href="/settings"
          className="rounded-lg border border-neutral-300 px-4 py-3 text-center font-semibold"
        >
          Diet settings
        </Link>
        <Link
          href="/restaurants"
          className="rounded-lg border border-neutral-300 px-4 py-3 text-center font-semibold"
        >
          Other restaurants
        </Link>
      </nav>
    </section>
  );
}
