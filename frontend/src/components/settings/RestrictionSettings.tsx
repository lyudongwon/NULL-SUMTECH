"use client";

import { useRouter } from "next/navigation";
import { btnOutline, btnPrimary, card, sectionLabel } from "@/components/ui/styles";
import { notifyRulesChanged } from "@/hooks/useMyRules";
import { useRestrictions } from "@/hooks/useRestrictions";
import { PresetPicker } from "./PresetPicker";
import { ToggleList } from "./ToggleList";

export function RestrictionSettings() {
  const router = useRouter();
  const { status, presets, items, preset, toggles, saving, message, selectPreset, toggle, save, reload } =
    useRestrictions();

  if (status === "loading") {
    return <p className="text-body">Loading…</p>;
  }
  if (status === "error") {
    return (
      <div className="flex flex-col items-start gap-3">
        <p role="alert" className="rounded-[14px] bg-avoid-bg px-4 py-3 text-sm font-medium text-avoid">
          {message}
        </p>
        <button type="button" onClick={reload} className={btnOutline}>
          Try again
        </button>
      </div>
    );
  }

  async function onSave() {
    if (await save()) {
      notifyRulesChanged();
      router.push("/");
    }
  }

  const selected = items.filter((item) => toggles[item.code]).length;

  return (
    <div className="flex flex-1 flex-col gap-6">
      <div className="flex flex-col gap-6 md:flex-row">
        <section className={`${card} flex flex-col gap-3 p-5 md:flex-1 md:self-start`}>
          <h2 className={sectionLabel}>Presets</h2>
          <PresetPicker presets={presets} selected={preset} onSelect={selectPreset} />
        </section>
        <section className={`${card} flex flex-col gap-3 p-5 md:flex-[2]`}>
          <div className="flex items-baseline justify-between">
            <h2 className={sectionLabel}>Foods I can&apos;t eat</h2>
            <span className="font-mono text-xs text-muted">{selected} selected</span>
          </div>
          <ToggleList items={items} toggles={toggles} onToggle={toggle} />
        </section>
      </div>

      <div className={`${card} flex flex-wrap items-center justify-between gap-x-6 gap-y-3 py-3.5 pr-3.5 pl-5`}>
        <span aria-live="polite" className="flex items-center gap-2 text-sm text-body">
          {message && (
            <span role="alert" className="font-medium text-avoid">
              {message}
            </span>
          )}
        </span>
        <button type="button" onClick={onSave} disabled={saving} className={`${btnPrimary} w-full sm:w-auto`}>
          {saving ? "Saving…" : "Save"}
        </button>
      </div>
    </div>
  );
}
