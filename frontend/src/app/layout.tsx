import type { Metadata, Viewport } from "next";
import { AppHeader } from "@/components/layout/AppHeader";
import { Icon } from "@/components/ui/icons";
import "./globals.css";

export const metadata: Metadata = {
  title: "Can I Eat?",
  description: "Snap a cafeteria menu or dish, and see if it fits your diet: Safe, Check first, or Avoid.",
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
};

// 글꼴을 못 받아도(오프라인 채점 등) 시스템 글꼴로 그대로 보인다. 그래서 빌드 때 받는 next/font 대신 링크로 둔다.
const FONTS =
  "https://fonts.googleapis.com/css2?family=IBM+Plex+Mono:wght@500&family=IBM+Plex+Sans:wght@400;500;600;700&display=swap";

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="flex min-h-dvh flex-col">
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="" />
        <link rel="stylesheet" href={FONTS} />
        <AppHeader />
        <main className="mx-auto flex w-full max-w-[1200px] flex-1 flex-col px-4 pt-8 pb-8 md:px-8 md:pt-10">
          {children}
        </main>
        {/* REQ-7 고지 문구: 모든 화면 하단에 항상 보인다 */}
        <footer className="border-t border-line bg-surface">
          <div className="mx-auto flex w-full max-w-[1200px] items-center gap-2 px-4 py-4 text-[13px] text-muted md:px-8">
            <Icon name="shield" size={16} />
            For guidance only. Please make the final check yourself or with a dietitian.
          </div>
        </footer>
      </body>
    </html>
  );
}
