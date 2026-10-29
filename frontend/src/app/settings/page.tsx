import Link from "next/link";
import { RestrictionSettings } from "@/components/settings/RestrictionSettings";

export const metadata = { title: "Diet settings | Can I Eat?" };

export default function SettingsPage() {
  return (
    <section className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">Diet settings</h1>
        <Link href="/" className="text-sm text-neutral-500">
          Home
        </Link>
      </div>
      <p className="text-sm text-neutral-600">
        Pick a preset or turn foods on and off, then save.
      </p>
      <RestrictionSettings />
    </section>
  );
}
