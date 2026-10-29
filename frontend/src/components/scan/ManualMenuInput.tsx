"use client";

import { useState, type FormEvent } from "react";
import { btnPrimary } from "@/components/ui/styles";

type Props = { disabled: boolean; onSubmit: (name: string) => void };

/** OPT-1 메뉴 이름 입력칸. 판독에 실패하면 나타난다. */
export function ManualMenuInput({ disabled, onSubmit }: Props) {
  const [name, setName] = useState("");

  function submit(event: FormEvent) {
    event.preventDefault();
    onSubmit(name);
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <label htmlFor="manual-menu-name" className="text-sm font-semibold text-muted">
        Menu name
      </label>
      <div className="flex flex-wrap gap-2">
        <input
          id="manual-menu-name"
          value={name}
          onChange={(event) => setName(event.target.value)}
          placeholder="e.g. 김치찌개"
          autoFocus
          autoComplete="off"
          className="h-12 min-w-0 flex-1 basis-48 rounded-xl border-[1.5px] border-olive bg-surface px-3.5 text-base"
        />
        <button type="submit" disabled={disabled} className={btnPrimary}>
          Can I eat it?
        </button>
      </div>
    </form>
  );
}
