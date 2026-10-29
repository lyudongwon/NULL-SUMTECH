"use client";

import { useCallback, useEffect, useState } from "react";
import { ApiError } from "@/lib/apiClient";
import { clearUserId, readUserId, saveUserId } from "@/lib/userId";
import * as authService from "@/services/authService";
import type { AuthUser } from "@/types/auth";

// 머리글(모든 화면에 남음)과 가입·로그인 양식이 각자 이 훅을 쓰므로, 양식이 로그인시키면 머리글에 알린다.
const CHANGED = "caniet:account-changed";

export type Account = { status: "loading" } | { status: "anonymous" } | { status: "authenticated"; user: AuthUser };

/**
 * OPT-4 로그인 상태. 서버의 세션 쿠키가 기준이고, 화면은 /auth/me로 묻는다.
 * 가입·로그인하면 계정의 user_id를 브라우저에 저장해 설정·판정·추천이 그 번호로 동작하게 한다.
 * 로그아웃하면 user_id를 지워 다음 사람이 이 계정의 식단 제한을 보지 못하게 한다.
 */
export function useAccount() {
  const [account, setAccount] = useState<Account>({ status: "loading" });

  useEffect(() => {
    let cancelled = false;
    authService
      .me()
      .then((user) => {
        if (cancelled) return;
        // 다른 탭에서 로그인했거나 저장소가 비어도 계정 번호를 맞춰 둔다.
        if (readUserId() !== user.user_id) saveUserId(user.user_id);
        setAccount({ status: "authenticated", user });
      })
      .catch(() => {
        // 401(로그인 안 함)이든 서버 오류든 로그인하지 않은 것으로 본다. 이전 번호는 건드리지 않는다.
        if (!cancelled) setAccount({ status: "anonymous" });
      });
    // 다른 곳(가입·로그인 양식)에서 로그인하면 바로 바꾼다.
    function onChanged(event: Event) {
      setAccount({ status: "authenticated", user: (event as CustomEvent<AuthUser>).detail });
    }
    window.addEventListener(CHANGED, onChanged);
    return () => {
      cancelled = true;
      window.removeEventListener(CHANGED, onChanged);
    };
  }, []);

  /** 가입 전에 쓰던 user_id를 함께 보내 식단 제한이 계정에 남게 한다. */
  const signup = useCallback(async (email: string, password: string) => {
    const previous = readUserId();
    const user = await authService.signup({ email, password, ...(previous !== null && { user_id: previous }) });
    saveUserId(user.user_id);
    setAccount({ status: "authenticated", user });
    window.dispatchEvent(new CustomEvent(CHANGED, { detail: user }));
    return user;
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const user = await authService.login({ email, password });
    saveUserId(user.user_id);
    setAccount({ status: "authenticated", user });
    window.dispatchEvent(new CustomEvent(CHANGED, { detail: user }));
    return user;
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch (error) {
      // 서버에 닿지 않아도 이 기기에서는 로그아웃한 것으로 한다.
      if (!(error instanceof ApiError)) throw error;
    }
    clearUserId();
    // 상태는 바꾸지 않는다. 호출한 쪽이 화면을 새로 열어(첫 화면 등) 저장된 식단 같은 것을 다시 읽게 하고,
    // 그 전에 로그아웃 모양이 먼저 보이면 곧바로 이어지는 새로 읽기가 그 사이의 클릭을 끊는다.
  }, []);

  return { account, signup, login, logout };
}
