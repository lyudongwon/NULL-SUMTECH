import Link from "next/link";
import { PageHead } from "@/components/layout/PageHead";
import { DietSummary } from "@/components/settings/DietSummary";
import { Icon } from "@/components/ui/icons";
import { LevelBadge } from "@/components/ui/LevelBadge";
import { btnOutline, btnPrimary, card, sectionLabel } from "@/components/ui/styles";
import type { Level } from "@/types/judgment";

// 첫 화면: 로그인 없이 서비스 이름과 하는 일이 보인다(progress.md 5단계).
const LEGEND: [Level, string][] = [
  ["SAFE", "No restricted ingredient, and hidden ones look unlikely."],
  ["CHECK", "Might hide one, like broth or sauce. Ask before you eat."],
  ["BLOCKED", "Contains something your diet excludes."],
];

export default function Home() {
  return (
    <div className="flex flex-1 flex-col gap-6">
      <PageHead
        title="Can I Eat?"
        lead="Snap a cafeteria menu or dish, and see if it fits your diet: Safe, Check first, or Avoid."
      />
      <DietSummary variant="home" />
      <div className="flex flex-1 flex-col gap-6 md:flex-row">
        <Link
          href="/scan"
          className="group flex flex-col items-center justify-center gap-[18px] rounded-3xl border-[1.5px] border-dashed border-[#c3c8d0] bg-surface px-5 pt-10 pb-8 text-center transition-colors hover:border-olive hover:bg-[#f7f9ef] md:flex-[2] md:pt-12"
        >
          <span className="flex h-16 w-16 items-center justify-center rounded-[18px] bg-chip text-olive">
            <Icon name="upload" size={30} stroke={1.7} />
          </span>
          <span className="flex flex-col gap-1.5">
            <span className="text-[19px] font-semibold">Upload one photo of a menu board or a dish.</span>
            <span className="text-sm text-muted">Take a photo · Choose from album</span>
          </span>
          <span className={`${btnPrimary} group-hover:bg-ink-hover`}>
            <Icon name="camera" />
            Upload photo
          </span>
        </Link>
        <aside className="flex min-w-0 flex-col gap-4 md:flex-1">
          <section className={`${card} flex flex-1 flex-col gap-[18px] p-6`}>
            <h2 className={sectionLabel}>What you&apos;ll get for each dish</h2>
            {LEGEND.map(([level, text]) => (
              <div key={level} className="flex flex-col items-start gap-1.5">
                <LevelBadge level={level} />
                <span className="text-sm leading-normal text-body">{text}</span>
              </div>
            ))}
            <Link href="/settings" className={`${btnOutline} mt-auto`}>
              Diet settings
            </Link>
          </section>
        </aside>
      </div>
    </div>
  );
}
