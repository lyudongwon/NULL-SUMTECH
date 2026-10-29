"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { AccountBar } from "@/components/auth/AccountBar";
import { Icon } from "@/components/ui/icons";

// 링크 이름은 docs/api.md > 화면 문구(머리글 메뉴)를 따른다. E2E가 이름으로 찾으므로 같은 화면에
// 같은 이름의 링크를 두지 않는다("Upload photo"·"Diet settings"는 첫 화면 본문에 있다).
// 가입·로그인 화면에는 양식 안에 같은 이름의 버튼·링크가 있어 머리글의 계정 버튼을 숨긴다.
const AUTH_PAGES = ["/login", "/signup"];

const NAV = [
  { href: "/", label: "Home" },
  { href: "/settings", label: "Restrictions" },
  { href: "/restaurants", label: "Other restaurants" },
];

export function AppHeader() {
  const pathname = usePathname();

  return (
    <header className="border-b border-line bg-surface">
      <div className="mx-auto flex w-full max-w-[1200px] flex-wrap items-center gap-x-4 gap-y-2 px-4 md:gap-x-8 py-3 md:px-8">
        <Link href="/" className="flex items-center gap-2.5 text-lg font-bold tracking-[-0.2px]">
          <span className="flex h-[30px] w-[30px] items-center justify-center rounded-lg bg-olive text-white">
            <Icon name="check" size={17} stroke={2.2} />
          </span>
          Can I Eat?
        </Link>
        <nav
          aria-label="Main"
          className="order-3 -mx-1 flex w-full gap-1 overflow-x-auto px-1 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden md:order-2 md:mx-0 md:w-auto md:flex-1 md:px-0"
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
        {/* 오른쪽 위: 가입·로그인(OPT-4). 로그인하면 이메일과 "Log out".
            휴대폰 폭에서 로그인했으면 이메일이 잘리지 않게 메뉴 아래 한 줄을 통째로 쓴다. */}
        <div className="order-2 ml-auto min-w-0 max-sm:has-[[data-account=signed-in]]:order-4 max-sm:has-[[data-account=signed-in]]:ml-0 max-sm:has-[[data-account=signed-in]]:w-full md:order-3 md:ml-0">
          {!AUTH_PAGES.includes(pathname) && <AccountBar />}
        </div>
      </div>
    </header>
  );
}

