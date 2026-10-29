import type { Metadata, Viewport } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "학식 식단 체커",
  description: "메뉴판·음식 사진 한 장으로 내 식단 제한에 맞는지 안전 / 확인 필요 / 불가로 알려 주는 서비스",
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
    <html lang="ko">
      <body className="flex min-h-dvh flex-col antialiased">
        <main className="mx-auto w-full max-w-md flex-1 px-4 py-6">{children}</main>
        {/* REQ-7 고지 문구: 모든 화면 하단에 항상 보인다 */}
        <footer className="border-t border-neutral-200 px-4 py-3 text-center text-xs text-neutral-500">
          판정 보조이며, 최종 확인은 본인과 영양사에게.
        </footer>
      </body>
    </html>
  );
}
