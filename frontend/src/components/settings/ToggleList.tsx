import type { RestrictionItem, Toggles } from "@/types/restriction";

type Props = {
  items: RestrictionItem[];
  toggles: Toggles;
  onToggle: (code: string) => void;
};

export function ToggleList({ items, toggles, onToggle }: Props) {
  return (
    <ul className="divide-y divide-neutral-200 rounded-lg border border-neutral-200">
      {items.map((item) => (
        <li key={item.code}>
          <label className="flex cursor-pointer items-center justify-between px-4 py-3">
            <span>{item.name}</span>
            <input
              type="checkbox"
              role="switch"
              aria-label={item.name}
              checked={!!toggles[item.code]}
              onChange={() => onToggle(item.code)}
              className="h-5 w-5 accent-emerald-600"
            />
          </label>
        </li>
      ))}
    </ul>
  );
}
