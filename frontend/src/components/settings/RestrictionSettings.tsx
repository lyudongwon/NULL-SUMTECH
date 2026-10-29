"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { btnOutline, btnPrimary, card, sectionLabel } from "@/components/ui/styles";
import { notifyDietChanged } from "@/hooks/useDietSummary";
import { useRestrictions } from "@/hooks/useRestrictions";
import { PresetPicker } from "./PresetPicker";
import { ToggleList } from "./ToggleList";

export function RestrictionSettings() {
  const router = useRouter();
  const { status, presets, items, preset, toggles, saving, message, needsLogin, selectPreset, toggle, save, reload } =
    useRestrictions();
  // 저장에 성공하면 첫 화면이 열릴 때까지 화면을 막는다(아래 onSave).
  const [leaving, setLeaving] = useState(false);

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
      // 저장이 끝나고 첫 화면으로 넘어갈 때까지 머리글 메뉴도 누르지 못하게 막는다.
      // 저장 중에 다른 화면으로 가면 저장 전 설정으로 판정·추천을 받거나, 늦게 온 이동에 첫 화면으로 끌려간다.
      setLeaving(true);
      notifyDietChanged();
      router.push("/");
    }
  }

  const selected = items.filter((item) => toggles[item.code]).length;

  return (
    <div aria-busy={saving || leaving} className="flex flex-1 flex-col gap-6">
      {(saving || leaving) && <div className="fixed inset-0 z-50 cursor-wait" />}
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
        <span aria-live="polite" className="flex flex-col gap-1 text-sm text-body">
          {/* 가입한 계정의 번호인데 이 브라우저가 로그인하지 않았다(OPT-4). */}
          {needsLogin && (
            <span>
              This diet belongs to an account.{" "}
              <Link href="/login" className="font-semibold text-olive hover:text-olive-dark">
                Log in
              </Link>{" "}
              to see and change it.
            </span>
          )}
          {message && (
            <span role="alert" className="font-medium text-avoid">
              {message}
            </span>
          )}
        </span>
        <button type="button" onClick={onSave} disabled={saving || leaving} className={`${btnPrimary} w-full sm:w-auto`}>
          {saving || leaving ? "Saving…" : "Save"}
        </button>
      </div>
    </div>
  );
}
