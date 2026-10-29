"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
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

  const field = "w-full rounded-lg border border-neutral-300 px-3 py-2";

  return (
    <form onSubmit={onSubmit} noValidate className="flex flex-col gap-4">
      <label className="flex flex-col gap-1 text-sm font-semibold text-neutral-700">
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
      <label className="flex flex-col gap-1 text-sm font-semibold text-neutral-700">
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
        <p role="alert" className="text-sm text-red-600">
          {message}
        </p>
      )}

      <button
        type="submit"
        disabled={busy}
        className="rounded-lg bg-emerald-600 px-4 py-3 font-semibold text-white disabled:opacity-60"
      >
        {busy ? "Please wait…" : copy.submit}
      </button>

      <p className="text-center text-sm text-neutral-600">
        {copy.other}{" "}
        <Link href={copy.otherHref} className="font-semibold text-emerald-700">
          {copy.otherLink}
        </Link>
      </p>
    </form>
  );
}
