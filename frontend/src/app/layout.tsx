import type { Metadata, Viewport } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Can I Eat?",
  description: "Snap a cafeteria menu or dish, and see if it fits your diet: Safe, Check first, or Avoid.",
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="flex min-h-dvh flex-col antialiased">
        <main className="mx-auto w-full max-w-md flex-1 px-4 py-6">{children}</main>
        {/* REQ-7 고지 문구: 모든 화면 하단에 항상 보인다 */}
        <footer className="border-t border-neutral-200 px-4 py-3 text-center text-xs text-neutral-500">
          For guidance only. Please make the final check yourself or with a dietitian.
        </footer>
      </body>
    </html>
  );
}
