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
            className={`rounded-full border px-4 py-2 text-sm font-medium ${
              active
                ? "border-emerald-600 bg-emerald-600 text-white"
                : "border-neutral-300 bg-white text-neutral-800"
            }`}
          >
            {preset.name}
          </button>
        );
      })}
    </div>
  );
}
