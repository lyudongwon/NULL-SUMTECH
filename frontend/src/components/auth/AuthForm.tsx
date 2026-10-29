"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { btnPrimary, card } from "@/components/ui/styles";
import { useAccount } from "@/hooks/useAccount";
import { ApiError } from "@/lib/apiClient";

type Props = { mode: "login" | "signup" };

const COPY = {
  login: { submit: "Log in", other: "New here?", otherLink: "Sign up", otherHref: "/signup" },
  signup: { submit: "Sign up", other: "Already have an account?", otherLink: "Log in", otherHref: "/login" },
} as const;

/** OPT-4 가입·로그인 양식. 성공하면 첫 화면으로 간다. */
export function AuthForm({ mode }: Props) {
  const router = useRouter();
  const { signup, login } = useAccount();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const copy = COPY[mode];

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (busy) return;
    setBusy(true);
    setMessage(null);
    try {
      await (mode === "signup" ? signup(email, password) : login(email, password));
      router.push("/");
    } catch (error) {
      setMessage(error instanceof ApiError ? error.detail : "Something went wrong. Please try again.");
      setBusy(false);
    }
  }

  const field = "h-12 w-full rounded-xl border border-edge bg-surface px-3.5 text-base font-normal text-ink";

  return (
    <form onSubmit={onSubmit} noValidate className={`${card} flex w-full max-w-md flex-col gap-4 p-6`}>
      <label className="flex flex-col gap-1.5 text-sm font-semibold text-muted">
        Email
        <input
          type="email"
          name="email"
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          className={field}
        />
      </label>
      <label className="flex flex-col gap-1.5 text-sm font-semibold text-muted">
        {mode === "signup" ? "Password (8+ characters)" : "Password"}
        <input
          type="password"
          name="password"
          autoComplete={mode === "signup" ? "new-password" : "current-password"}
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          className={field}
        />
      </label>

      {message && (
        <p role="alert" className="rounded-[14px] bg-avoid-bg px-4 py-3 text-sm font-medium text-avoid">
          {message}
        </p>
      )}

      <button
        type="submit"
        disabled={busy}
        className={btnPrimary}
      >
        {busy ? "Please wait…" : copy.submit}
      </button>

      <p className="text-center text-sm text-body">
        {copy.other}{" "}
        <Link href={copy.otherHref} className="font-semibold text-olive hover:text-olive-dark">
          {copy.otherLink}
        </Link>
      </p>
    </form>
  );
}
