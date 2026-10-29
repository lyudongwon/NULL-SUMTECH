import type { Level } from "@/types/judgment";
import { Icon, type IconName } from "./icons";

// 배지 문구는 docs/api.md > 화면 문구(Safe · Check first · Avoid)를 따른다.
export const LEVEL_LABEL: Record<Level, string> = {
  SAFE: "Safe",
  CHECK: "Check first",
  BLOCKED: "Avoid",
};

const ICON: Record<Level, IconName> = { SAFE: "check", CHECK: "bang", BLOCKED: "x" };

const TONE: Record<Level, { soft: string; solid: string }> = {
  SAFE: { soft: "bg-safe-bg text-safe", solid: "bg-safe text-white" },
  CHECK: { soft: "bg-check-bg text-check", solid: "bg-check text-white" },
  BLOCKED: { soft: "bg-avoid-bg text-avoid", solid: "bg-avoid text-white" },
};

/** 문구는 span의 바로 아래 글자로 둔다(E2E가 getByText("Avoid", { exact: true })로 찾는다). */
export function LevelBadge({ level, solid = false }: { level: Level; solid?: boolean }) {
  return (
    <span
      className={`inline-flex shrink-0 items-center gap-1 whitespace-nowrap rounded-full py-[3px] pr-[9px] pl-[7px] text-[12.5px] font-semibold ${
        solid ? TONE[level].solid : TONE[level].soft
      }`}
    >
      <Icon name={ICON[level]} size={12} stroke={3.2} />
      {LEVEL_LABEL[level]}
    </span>
  );
}
