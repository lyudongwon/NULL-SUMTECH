"use client";

import { useRouter } from "next/navigation";
import { useRestrictions } from "@/hooks/useRestrictions";
import { PresetPicker } from "./PresetPicker";
import { ToggleList } from "./ToggleList";

export function RestrictionSettings() {
  const router = useRouter();
  const { status, presets, items, preset, toggles, saving, message, selectPreset, toggle, save, reload } =
    useRestrictions();

  if (status === "loading") {
    return <p className="text-neutral-500">불러오는 중…</p>;
  }
  if (status === "error") {
    return (
      <div className="flex flex-col gap-3">
        <p role="alert" className="text-red-600">
          {message}
        </p>
        <button type="button" onClick={reload} className="self-start rounded-lg border px-4 py-2">
          다시 시도
        </button>
      </div>
    );
  }

  async function onSave() {
    if (await save()) router.push("/");
  }

  return (
    <div className="flex flex-col gap-6">
      <section className="flex flex-col gap-2">
        <h2 className="text-sm font-semibold text-neutral-600">프리셋</h2>
        <PresetPicker presets={presets} selected={preset} onSelect={selectPreset} />
      </section>

      <section className="flex flex-col gap-2">
        <h2 className="text-sm font-semibold text-neutral-600">못 먹는 음식</h2>
        <ToggleList items={items} toggles={toggles} onToggle={toggle} />
      </section>

      {message && (
        <p role="alert" className="text-sm text-red-600">
          {message}
        </p>
      )}

      <button
        type="button"
        onClick={onSave}
        disabled={saving}
        className="rounded-lg bg-emerald-600 px-4 py-3 font-semibold text-white disabled:opacity-60"
      >
        {saving ? "저장 중…" : "저장"}
      </button>
    </div>
  );
}
