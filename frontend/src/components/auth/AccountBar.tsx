"use client";

import Link from "next/link";
import { btnOutline, btnPrimary } from "@/components/ui/styles";
import { useAccount } from "@/hooks/useAccount";

/** 첫 화면 제목줄 오른쪽: 로그인하지 않았으면 "Log in"·"Sign up", 로그인했으면 이메일과 "Log out". */
// 시안의 작은 버튼(.btn-sm)
const SMALL = "h-10 rounded-[10px] px-3.5 text-[13px]";

export function AccountBar() {
  const { account, logout } = useAccount();

  if (account.status === "loading") {
    // 자리를 비워 둬 화면이 밀리지 않게 한다.
    return <div className="h-10" aria-hidden />;
  }

  if (account.status === "authenticated") {
    return (
      <div className="flex min-w-0 items-center gap-3 text-sm">
        <span className="truncate text-body">{account.user.email}</span>
        <button
          type="button"
          onClick={async () => {
            await logout();
            // 화면 곳곳이 저장된 식단을 한 번씩 읽어 두므로 새로 읽게 한다.
            window.location.assign("/");
          }}
          className={`${btnOutline} ${SMALL} shrink-0`}
        >
          Log out
        </button>
      </div>
    );
  }

  return (
    <div className="flex items-center gap-2">
      <Link href="/login" className={`${btnOutline} ${SMALL}`}>
        Log in
      </Link>
      <Link href="/signup" className={`${btnPrimary} ${SMALL}`}>
        Sign up
      </Link>
    </div>
  );
}
