import { PageHead } from "@/components/layout/PageHead";
import { RestrictionSettings } from "@/components/settings/RestrictionSettings";

export const metadata = { title: "Diet settings | Can I Eat?" };

export default function SettingsPage() {
  return (
    <div className="flex flex-1 flex-col gap-6">
      <PageHead title="Diet settings" lead="Pick a preset or turn foods on and off, then save." />
      <RestrictionSettings />
    </div>
  );
}
