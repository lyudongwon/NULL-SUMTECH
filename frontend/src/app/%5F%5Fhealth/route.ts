// GET /__health — 인증·리다이렉트 없이 200을 준다. releaseId는 배포된 commit(RELEASE_ID)이다.
// 밑줄로 시작하는 폴더는 App Router에서 숨김 처리되므로 폴더 이름을 %5F%5Fhealth로 쓴다.
export const dynamic = "force-dynamic";

export function GET() {
  return Response.json(
    { status: "ok", releaseId: process.env.RELEASE_ID ?? null },
    // 대시보드가 이전 배포의 응답을 캐시해서 보지 않게 한다.
    { headers: { "Cache-Control": "no-store" } },
  );
}
