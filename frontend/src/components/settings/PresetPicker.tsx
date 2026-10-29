import type { Preset, PresetCode } from "@/types/restriction";

type Props = {
  presets: Preset[];
  selected: PresetCode | null;
  onSelect: (code: PresetCode) => void;
};

export function PresetPicker({ presets, selected, onSelect }: Props) {
  return (
    <div className="flex flex-wrap gap-2">
      {presets.map((preset) => {
        const active = preset.code === selected;
        return (
          <button
            key={preset.code}
            type="button"
            aria-pressed={active}
            onClick={() => onSelect(preset.code)}
            className={`min-h-11 rounded-full border px-4 text-sm ${
              active ? "border-ink bg-ink font-semibold text-white" : "border-edge bg-surface font-medium text-ink"
            }`}
          >
            {preset.name}
          </button>
        );
      })}
    </div>
  );
}
