import Link from "next/link";
import { Icon } from "@/components/ui/icons";

/** OPT-2 "Other restaurants" 버튼. 첫 화면과 판정 화면(먹을 메뉴가 없을 때)에 둔다. */
export function OtherRestaurantsCta() {
  return (
    <Link
      href="/restaurants"
      className="flex w-full items-center gap-3 rounded-2xl bg-ink px-[18px] py-4 text-left text-white hover:bg-ink-hover"
    >
      <span className="text-[#dde3ea]">
        <Icon name="pin" size={22} />
      </span>
      <span className="flex flex-1 flex-col gap-0.5">
        <span className="text-[15px] font-semibold">Other restaurants</span>
        <span className="text-[13px] text-[#c9ceda]">Restaurants that fit your diet, for when the cafeteria doesn&apos;t.</span>
      </span>
      <Icon name="chev" />
    </Link>
  );
}
