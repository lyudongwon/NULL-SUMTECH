import type { RestrictionItem, Toggles } from "@/types/restriction";

type Props = {
  items: RestrictionItem[];
  toggles: Toggles;
  onToggle: (code: string) => void;
};

/** 시안의 스위치 모양. 실제 입력은 체크박스(role=switch)라서 E2E의 check()·toBeChecked()가 그대로 동작한다. */
export function ToggleList({ items, toggles, onToggle }: Props) {
  return (
    <div className="grid grid-cols-1 gap-1.5 sm:grid-cols-2">
      {items.map((item) => {
        const on = !!toggles[item.code];
        return (
          <label
            key={item.code}
            className={`flex min-h-[52px] cursor-pointer items-center gap-3 rounded-[14px] border px-3.5 py-1.5 ${
              on ? "border-[#c9d19a] bg-olive-tint" : "border-line bg-surface"
            }`}
          >
            <span className="flex-1 text-[15px] font-semibold">{item.name}</span>
            {/* 입력을 스위치 위에 투명하게 겹친다. 눌리는 대상이 입력 자체라서 E2E의 check()가 막히지 않는다. */}
            <span className="relative flex shrink-0">
              <input
                type="checkbox"
                role="switch"
                aria-label={item.name}
                checked={on}
                onChange={() => onToggle(item.code)}
                className="peer absolute inset-0 z-10 m-0 h-full w-full cursor-pointer opacity-0"
              />
              <span
                aria-hidden="true"
                className="flex h-6 w-10 items-center rounded-full bg-edge px-[3px] transition-colors peer-checked:bg-olive peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-olive after:h-[18px] after:w-[18px] after:rounded-full after:bg-white after:transition-transform after:content-[''] peer-checked:after:translate-x-4"
              />
            </span>
          </label>
        );
      })}
    </div>
  );
}
