"use client";

import { useCallback, useEffect, useState } from "react";
import { ApiError } from "@/lib/apiClient";
import { clearUserId, readUserId } from "@/lib/userId";
import { getPresets, getRestrictions, saveRestrictions } from "@/services/restrictionService";
import type { Preset, PresetCode, RestrictionItem, Toggles } from "@/types/restriction";
import { ensureUserId } from "./userIdentity";

type Status = "loading" | "ready" | "error";

/** REQ-4 설정 화면 상태. 프리셋·토글은 화면에서만 바꾸고 "저장"을 누를 때 한 번에 보낸다. */
export function useRestrictions() {
  const [status, setStatus] = useState<Status>("loading");
  const [presets, setPresets] = useState<Preset[]>([]);
  const [items, setItems] = useState<RestrictionItem[]>([]);
  const [preset, setPreset] = useState<PresetCode | null>(null);
  const [toggles, setToggles] = useState<Toggles>({});
  const [version, setVersion] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const load = useCallback(async () => {
    setStatus("loading");
    try {
      const list = await getPresets();
      setPresets(list.presets);
      setItems(list.items);
      setPreset(null);
      setToggles({});
      setVersion(null);

      const userId = readUserId();
      if (userId) {
        try {
          const saved = await getRestrictions(userId);
          setPreset(saved.preset);
          setToggles(saved.toggles);
          setVersion(saved.version);
        } catch (error) {
          // 404: 아직 저장한 적 없음 → 기본값(제한 없음)
          if (!(error instanceof ApiError && error.status === 404)) throw error;
        }
      }
      setStatus("ready");
    } catch (error) {
      setMessage(error instanceof ApiError ? error.detail : "설정을 불러오지 못했습니다.");
      setStatus("error");
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  function selectPreset(code: PresetCode) {
    const chosen = presets.find((p) => p.code === code);
    if (!chosen) return;
    setPreset(code);
    setToggles(Object.fromEntries(items.map((item) => [item.code, chosen.items.includes(item.code)])));
    setMessage(null);
  }

  function toggle(code: string) {
    setToggles((prev) => ({ ...prev, [code]: !prev[code] }));
    setMessage(null);
  }

  /** 저장에 성공하면 true. 다른 요청이 먼저 저장했으면(409) 다시 불러온다. */
  async function save(): Promise<boolean> {
    if (saving) return false;
    setSaving(true);
    setMessage(null);
    try {
      const body = { preset, toggles: Object.fromEntries(items.map((i) => [i.code, !!toggles[i.code]])) };
      let userId = await ensureUserId();
      try {
        const saved = await saveRestrictions(userId, { ...body, ...(version !== null && { version }) });
        setVersion(saved.version);
      } catch (error) {
        // 브라우저에 남은 user_id가 서버에 없으면(DB 초기화 등) 새로 발급받아 처음 저장으로 다시 보낸다.
        if (!(error instanceof ApiError && error.status === 404)) throw error;
        clearUserId();
        userId = await ensureUserId();
        const saved = await saveRestrictions(userId, body);
        setVersion(saved.version);
      }
      return true;
    } catch (error) {
      if (error instanceof ApiError && error.status === 409) {
        await load();
      }
      setMessage(error instanceof ApiError ? error.detail : "저장하지 못했습니다.");
      return false;
    } finally {
      setSaving(false);
    }
  }

  return { status, presets, items, preset, toggles, saving, message, selectPreset, toggle, save, reload: load };
}
