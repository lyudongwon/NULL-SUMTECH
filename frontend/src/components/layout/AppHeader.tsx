"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useDietSummary, type DietSummary } from "@/hooks/useDietSummary";
import { Icon } from "@/components/ui/icons";

// 링크 이름은 docs/api.md > 화면 문구(머리글 메뉴)를 따른다. E2E가 이름으로 찾으므로 같은 화면에
// 같은 이름의 링크를 두지 않는다("Upload photo"·"Diet settings"는 첫 화면 본문에 있다).
const NAV = [
  { href: "/", label: "Home" },
  { href: "/settings", label: "Restrictions" },
  { href: "/restaurants", label: "Other restaurants" },
];

export function AppHeader() {
  const pathname = usePathname();
  const summary = useDietSummary();

  return (
    <header className="border-b border-line bg-surface">
      <div className="mx-auto flex w-full max-w-[1200px] flex-wrap items-center gap-x-8 gap-y-2 px-4 py-3 md:px-8">
        <Link href="/" className="flex items-center gap-2.5 text-lg font-bold tracking-[-0.2px]">
          <span className="flex h-[30px] w-[30px] items-center justify-center rounded-lg bg-olive text-white">
            <Icon name="check" size={17} stroke={2.2} />
          </span>
          Can I Eat?
        </Link>
        <nav
          aria-label="Main"
          className="order-3 -mx-1 flex w-full gap-1 overflow-x-auto px-1 md:order-2 md:mx-0 md:w-auto md:flex-1 md:px-0"
        >
          {NAV.map(({ href, label }) => {
            const current = href === "/" ? pathname === "/" : pathname.startsWith(href);
            return (
              <Link
                key={href}
                href={href}
                aria-current={current ? "page" : undefined}
                className={`flex min-h-10 shrink-0 items-center rounded-[10px] px-3.5 text-sm ${
                  current ? "bg-divider font-semibold text-ink" : "font-medium text-body"
                }`}
              >
                {label}
              </Link>
            );
          })}
        </nav>
        <Link
          href="/settings"
          className="order-2 ml-auto flex min-h-10 items-center gap-2 rounded-full border border-edge bg-surface px-3.5 text-[13px] md:order-3 md:ml-0"
        >
          <span className={`h-2 w-2 rounded-full ${summary?.status === "set" ? "bg-olive" : "bg-[#b4bac4]"}`} />
          <span className="text-muted">My rules</span>
          <strong className="font-semibold">{rulesLabel(summary)}</strong>
        </Link>
      </div>
    </header>
  );
}

/** 프리셋 이름, 없으면 켠 항목(세 개까지 + 나머지 수). 저장 전이거나 켠 항목이 없으면 "Not set" */
function rulesLabel(summary: DietSummary | null) {
  if (summary?.status !== "set") return "Not set";
  if (summary.presetName) return summary.presetName;
  const names = summary.itemNames;
  return names.length > 3 ? `${names.slice(0, 3).join(" · ")} +${names.length - 3}` : names.join(" · ");
}
