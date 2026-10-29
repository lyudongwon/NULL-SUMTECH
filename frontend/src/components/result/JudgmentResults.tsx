"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { PageHead } from "@/components/layout/PageHead";
import { OtherRestaurantsCta } from "@/components/restaurants/OtherRestaurantsCta";
import { EmptyInputDialog } from "@/components/scan/ScanFlow";
import { DietSummary } from "@/components/settings/DietSummary";
import { LEVEL_LABEL } from "@/components/ui/LevelBadge";
import { btnOutline, btnPrimary, card } from "@/components/ui/styles";
import { parseMenus, useJudgment } from "@/hooks/useJudgment";
import type { Level } from "@/types/judgment";
import { ResultCard } from "./ResultCard";

const COUNT_TONE: Record<Level, string> = { BLOCKED: "text-avoid", CHECK: "text-check", SAFE: "text-safe" };

export function JudgmentResults() {
  const router = useRouter();
  const params = useSearchParams();
  const menus = parseMenus(params.get("menus"));
  // ③의 판정 API가 나오기 전 화면 개발용. 개발 서버에서만 ?demo=1을 받는다.
  const demo = process.env.NODE_ENV !== "production" && params.get("demo") === "1";
  const { state, retry } = useJudgment(menus, demo);

  const retake = (
    <Link href="/scan" className={btnPrimary}>
      Retake
    </Link>
  );
  // 제목줄 안내에는 판정에 쓴 식단("Checked for: …")을 모든 상태에서 보여 준다.
  const head = (title: string) => <PageHead title={title} lead={<DietSummary variant="result" />} actions={retake} />;

  if (state.status === "loading") {
    return (
      <div className="flex flex-col gap-6">
        {head("Results")}
        <p role="status" className="flex items-center gap-3 text-body">
          <span className="h-5 w-5 animate-spin rounded-full border-[2.5px] border-edge border-t-olive" />
          Checking…
        </p>
      </div>
    );
  }

  // 판독 없이 /result를 바로 열었을 때(메뉴 값 자체가 없음)는 오류가 아니라 빈 화면 안내를 보여 준다.
  // 메뉴 이름을 비워 보낸 경우만 "Please enter a menu name." 대화상자를 띄운다(api.md REQ-3 입력 값 없음).
  if (state.status === "empty" && params.get("menus") === null) {
    return (
      <div className="flex flex-col gap-6">
        <PageHead title="Results" lead={<DietSummary variant="result" />} />
        <div className={`${card} flex flex-col items-start gap-3 p-6`}>
          <p className="text-[17px] font-semibold">No results yet</p>
          <p className="text-[15px] text-body">Upload a photo of a menu board or a dish, and we&apos;ll check each menu for you.</p>
          <Link href="/scan" className={btnPrimary}>
            Upload photo
          </Link>
        </div>
      </div>
    );
  }

  if (state.status === "empty") {
    return (
      <div className="flex flex-col gap-6">
        {head("Results")}
        <EmptyInputDialog onClose={() => router.push("/scan")} />
      </div>
    );
  }

  if (state.status === "error") {
    return (
      <div className="flex flex-col gap-6">
        {head("Results")}
        <p role="alert" className="rounded-[14px] bg-avoid-bg px-4 py-3 text-sm font-medium text-avoid">
          {state.message}
        </p>
        {/* 판정이 실패해도(AI 지연·서버 오류) 다시 판정할 수 있게 한다. "Retake"는 제목줄에 있다. */}
        <button type="button" onClick={retry} className={`${btnOutline} self-start`}>
          Try again
        </button>
      </div>
    );
  }

  const count = (level: Level) => state.results.filter((r) => r.level === level).length;
  const noSafe = count("SAFE") === 0;

  return (
    <div className="flex flex-col gap-6">
      {head("Results")}
      <div className="grid grid-cols-3 gap-2">
        {(["BLOCKED", "CHECK", "SAFE"] as Level[]).map((level) => (
          <div
            key={level}
            className={`flex flex-col gap-1 rounded-[14px] bg-surface px-4 py-3.5 text-[13px] font-semibold sm:flex-row sm:items-baseline sm:justify-between ${COUNT_TONE[level]}`}
          >
            <span>{LEVEL_LABEL[level]}</span>
            <span className="font-mono text-2xl font-medium">{count(level)}</span>
          </div>
        ))}
      </div>
      <p className="-mb-3 text-[13px] text-muted">Tap a menu to see why.</p>
      <ul className={`${card} overflow-hidden`}>
        {state.results.map((result) => (
          <ResultCard key={result.menu_name} result={result} itemNames={state.itemNames} />
        ))}
      </ul>
      {noSafe && <OtherRestaurantsCta />}
    </div>
  );
}
