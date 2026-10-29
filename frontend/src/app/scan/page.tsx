import Link from "next/link";
import { ScanFlow } from "@/components/scan/ScanFlow";

export const metadata = { title: "Upload photo | Can I Eat?" };

export default function ScanPage() {
  return (
    <section className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">Upload photo</h1>
        <Link href="/" className="text-sm text-neutral-500">
          Home
        </Link>
      </div>
      <ScanFlow />
    </section>
  );
}
