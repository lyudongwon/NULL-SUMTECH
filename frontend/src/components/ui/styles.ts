// 시안의 버튼·카드 모양. 여러 화면이 같이 쓴다.
export const btn =
  "inline-flex h-12 items-center justify-center gap-2 rounded-xl border px-[22px] text-[15px] font-semibold disabled:cursor-default disabled:border-[#8d96a8] disabled:bg-[#8d96a8] disabled:text-white";
export const btnPrimary = `${btn} border-ink bg-ink text-white hover:bg-ink-hover`;
export const btnOutline = `${btn} border-edge bg-surface text-ink`;
export const btnOlive = `${btn} border-olive bg-olive text-white`;
export const card = "rounded-[18px] bg-surface";
export const sectionLabel = "text-sm font-semibold text-muted";
