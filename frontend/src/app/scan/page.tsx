import { PageHead } from "@/components/layout/PageHead";
import { ScanFlow } from "@/components/scan/ScanFlow";

export const metadata = { title: "Upload photo | Can I Eat?" };

export default function ScanPage() {
  return (
    <div className="flex flex-1 flex-col gap-6">
      <PageHead title="Upload photo" />
      <ScanFlow />
    </div>
  );
}
