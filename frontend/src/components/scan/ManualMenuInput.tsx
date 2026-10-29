"use client";

import { useState, type FormEvent } from "react";

type Props = { disabled: boolean; onSubmit: (name: string) => void };

/** OPT-1 메뉴 이름 입력칸. 판독에 실패하면 나타난다. */
export function ManualMenuInput({ disabled, onSubmit }: Props) {
  const [name, setName] = useState("");

  function submit(event: FormEvent) {
    event.preventDefault();
    onSubmit(name);
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-2">
      <label htmlFor="manual-menu-name" className="text-sm font-semibold text-neutral-600">
        메뉴 이름
      </label>
      <div className="flex gap-2">
        <input
          id="manual-menu-name"
          value={name}
          onChange={(event) => setName(event.target.value)}
          placeholder="예: 김치찌개"
          autoFocus
          className="min-w-0 flex-1 rounded-lg border border-neutral-300 px-3 py-2"
        />
        <button
          type="submit"
          disabled={disabled}
          className="rounded-lg bg-emerald-600 px-4 py-2 font-semibold text-white disabled:opacity-60"
        >
          판정하기
        </button>
      </div>
    </form>
  );
}
