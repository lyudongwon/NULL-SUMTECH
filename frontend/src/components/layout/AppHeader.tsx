"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useMyRules } from "@/hooks/useMyRules";
import { Icon } from "@/components/ui/icons";

// 링크 이름은 docs/api.md > 화면 문구(머리글 메뉴)를 따른다.
// "Upload photo"·"Diet settings"는 첫 화면 본문에만 둔다. 저장 뒤 첫 화면으로 넘어가야 보이는 링크라서
// 흐름(E2E)이 저장이 끝나기를 기다리는 기준이 된다. 머리글에 같은 이름을 두면 저장 중에 앞질러 누른다.
const NAV = [
  { href: "/", label: "Home" },
  { href: "/scan", label: "Photo check" },
  { href: "/settings", label: "Restrictions" },
];

export function AppHeader() {
  const pathname = usePathname();
  const rules = useMyRules();

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
          <span className={`h-2 w-2 rounded-full ${rules.saved ? "bg-olive" : "bg-[#b4bac4]"}`} />
          <span className="text-muted">My rules</span>
          <strong className="font-semibold">{rules.label ?? "Not set"}</strong>
        </Link>
      </div>
    </header>
  );
}
