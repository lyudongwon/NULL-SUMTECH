"use client";

import { useState, type FormEvent } from "react";
import { btnOutline, btnPrimary } from "@/components/ui/styles";
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
// 시안의 작은 버튼(.btn-sm)
const SMALL = "h-10 rounded-[10px] px-3.5 text-[13px]";

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
        className={`${btnOutline} ${SMALL} mt-3 self-start`}
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

  const field = "h-10 w-full rounded-[10px] border border-edge bg-surface px-3 text-sm font-normal text-ink";

  return (
    <form onSubmit={onSubmit} className="mt-3 flex flex-col gap-3 border-t border-divider pt-3">
      <label className="flex flex-col gap-1 text-xs font-semibold text-muted">
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
      <label className="flex flex-col gap-1 text-xs font-semibold text-muted">
        Dish name (optional)
        <input
          value={menuName}
          maxLength={100}
          onChange={(event) => setMenuName(event.target.value)}
          className={field}
        />
      </label>
      <label className="flex flex-col gap-1 text-xs font-semibold text-muted">
        Note (optional)
        <input value={note} maxLength={500} onChange={(event) => setNote(event.target.value)} className={field} />
      </label>

      {message && (
        <span role="alert" className="text-sm font-medium text-avoid">
          {message}
        </span>
      )}

      <div className="flex gap-2">
        <button
          type="submit"
          disabled={busy}
          className={`${btnPrimary} ${SMALL} flex-1`}
        >
          {busy ? "Please wait…" : "Send"}
        </button>
        <button
          type="button"
          onClick={() => {
            setOpen(false);
            setMessage(null);
          }}
          className={`${btnOutline} ${SMALL} flex-1`}
        >
          Cancel
        </button>
      </div>
    </form>
  );
}
