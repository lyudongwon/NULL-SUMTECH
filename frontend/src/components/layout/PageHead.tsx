import type { ReactNode } from "react";

/** 모든 화면이 같은 위치·크기로 쓰는 제목줄(시안의 .phead). */
export function PageHead({ title, lead, actions }: { title: ReactNode; lead?: ReactNode; actions?: ReactNode }) {
  return (
    <header className="flex flex-wrap items-end justify-between gap-x-4 gap-y-3">
      <div className="flex min-w-0 flex-col gap-1.5">
        <h1 className="text-[28px] leading-tight font-bold tracking-[-0.6px] md:text-[32px]">{title}</h1>
        {lead && <p className="text-[15px] leading-relaxed text-body">{lead}</p>}
      </div>
      {actions && <div className="flex flex-wrap gap-2">{actions}</div>}
    </header>
  );
}
