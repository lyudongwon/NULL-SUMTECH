"use client";

import { useState, type FormEvent } from "react";
import { ApiError } from "@/lib/apiClient";
import { submitFeedback } from "@/services/restaurantService";
import type { RestrictionItem } from "@/types/restriction";

type Props = {
  restaurantId: number;
  items: RestrictionItem[];
  /** 제보를 보냈을 때. 보낸 항목을 알려 준다(되돌리기에 쓴다). */
  onSent: (item: RestrictionItem) => void;
};

/** OPT-5 "Report an ingredient": 이 가게에서 쓰는 항목과 음식 이름·메모를 보낸다. 로그인한 사용자만 이 양식을 볼 수 있다. */
export function FeedbackForm({ restaurantId, items, onSent }: Props) {
  const [open, setOpen] = useState(false);
  const [itemCode, setItemCode] = useState("");
  const [menuName, setMenuName] = useState("");
  const [note, setNote] = useState("");
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  if (!open) {
    return (
      <button
        type="button"
        onClick={() => setOpen(true)}
        className="mt-3 rounded-lg border border-neutral-300 px-3 py-1 text-sm font-semibold"
      >
        Report an ingredient
      </button>
    );
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (busy) return;
    const item = items.find((i) => i.code === itemCode);
    if (!item) {
      setMessage("Please choose an ingredient.");
      return;
    }
    setBusy(true);
    setMessage(null);
    try {
      await submitFeedback(restaurantId, {
        item_code: item.code,
        ...(menuName.trim() && { menu_name: menuName.trim() }),
        ...(note.trim() && { note: note.trim() }),
      });
      setOpen(false);
      onSent(item);
    } catch (error) {
      setMessage(error instanceof ApiError ? error.detail : "Couldn't send your report.");
      setBusy(false);
    }
  }

  const field = "w-full rounded-lg border border-neutral-300 px-3 py-2 text-sm";

  return (
    <form onSubmit={onSubmit} className="mt-3 flex flex-col gap-3 border-t border-neutral-200 pt-3">
      <label className="flex flex-col gap-1 text-xs font-semibold text-neutral-700">
        Which ingredient is used?
        <select value={itemCode} onChange={(event) => setItemCode(event.target.value)} className={field}>
          <option value="" disabled>
            Choose…
          </option>
          {items.map((item) => (
            <option key={item.code} value={item.code}>
              {item.name}
            </option>
          ))}
        </select>
      </label>
      <label className="flex flex-col gap-1 text-xs font-semibold text-neutral-700">
        Dish name (optional)
        <input
          value={menuName}
          maxLength={100}
          onChange={(event) => setMenuName(event.target.value)}
          className={field}
        />
      </label>
      <label className="flex flex-col gap-1 text-xs font-semibold text-neutral-700">
        Note (optional)
        <input value={note} maxLength={500} onChange={(event) => setNote(event.target.value)} className={field} />
      </label>

      {message && (
        <span role="alert" className="text-sm text-red-600">
          {message}
        </span>
      )}

      <div className="flex gap-2">
        <button
          type="submit"
          disabled={busy}
          className="flex-1 rounded-lg bg-emerald-600 px-3 py-2 text-sm font-semibold text-white disabled:opacity-60"
        >
          {busy ? "Please wait…" : "Send"}
        </button>
        <button
          type="button"
          onClick={() => {
            setOpen(false);
            setMessage(null);
          }}
          className="flex-1 rounded-lg border border-neutral-300 px-3 py-2 text-sm font-semibold"
        >
          Cancel
        </button>
      </div>
    </form>
  );
}
