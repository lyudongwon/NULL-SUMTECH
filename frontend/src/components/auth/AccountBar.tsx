"use client";

import Link from "next/link";
import { useAccount } from "@/hooks/useAccount";

/** 첫 화면 위쪽: 로그인하지 않았으면 "Log in"·"Sign up", 로그인했으면 이메일과 "Log out". */
export function AccountBar() {
  const { account, logout } = useAccount();

  if (account.status === "loading") {
    // 자리를 비워 둬 화면이 밀리지 않게 한다.
    return <div className="h-8" aria-hidden />;
  }

  if (account.status === "authenticated") {
    return (
      <div className="flex items-center justify-between gap-3 text-sm">
        <span className="truncate text-neutral-600">{account.user.email}</span>
        <button
          type="button"
          onClick={async () => {
            await logout();
            // 화면 곳곳이 저장된 식단을 한 번씩 읽어 두므로 새로 읽게 한다.
            window.location.assign("/");
          }}
          className="shrink-0 rounded-lg border border-neutral-300 px-3 py-1 font-semibold"
        >
          Log out
        </button>
      </div>
    );
  }

  return (
    <div className="flex items-center justify-end gap-2 text-sm">
      <Link href="/login" className="rounded-lg border border-neutral-300 px-3 py-1 font-semibold">
        Log in
      </Link>
      <Link href="/signup" className="rounded-lg bg-emerald-600 px-3 py-1 font-semibold text-white">
        Sign up
      </Link>
    </div>
  );
}
