"use client";

import Link from "next/link";
import { btnOutline, btnPrimary } from "@/components/ui/styles";
import { useAccount } from "@/hooks/useAccount";

/** 머리글 오른쪽 위: 로그인하지 않았으면 "Log in"·"Sign up", 로그인했으면 이메일과 "Log out". */
// 시안의 작은 버튼(.btn-sm). 휴대폰 폭에서는 로고와 한 줄에 들어가도록 조금 더 줄인다.
const SMALL = "h-9 rounded-[10px] px-3 text-[13px] sm:h-10 sm:px-3.5";

export function AccountBar() {
  const { account, logout } = useAccount();

  if (account.status === "loading") {
    // 자리를 비워 둬 화면이 밀리지 않게 한다.
    return <div className="h-9 sm:h-10" aria-hidden />;
  }

  if (account.status === "authenticated") {
    return (
      <div data-account="signed-in" className="flex min-w-0 items-center justify-between gap-2 text-sm sm:justify-start sm:gap-3">
        <span title={account.user.email} className="min-w-0 truncate text-[13px] text-body sm:max-w-64">
          {account.user.email}
        </span>
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
