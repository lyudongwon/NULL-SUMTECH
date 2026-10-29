// GET /__health — 인증·리다이렉트 없이 200을 준다. releaseId는 배포된 commit이다.
// 밑줄로 시작하는 폴더는 App Router에서 숨김 처리되므로 폴더 이름을 %5F%5Fhealth로 쓴다.
export const dynamic = "force-dynamic";

// Render는 배포한 commit을 RENDER_GIT_COMMIT으로 넣어 준다. docker compose 실행(채점·CI)에는 없으니 RELEASE_ID를 쓴다.
function releaseId() {
  return process.env.RENDER_GIT_COMMIT || process.env.RELEASE_ID || null;
}

export function GET() {
  return Response.json(
    { status: "ok", releaseId: releaseId() },
    // 대시보드가 이전 배포의 응답을 캐시해서 보지 않게 한다.
    { headers: { "Cache-Control": "no-store" } },
  );
}
