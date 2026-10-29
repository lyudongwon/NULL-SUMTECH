// 브라우저의 /api/v1/** 요청을 백엔드(Spring Boot)로 그대로 넘긴다.
// 같은 출처로 부르니 CORS가 필요 없고, BACKEND_URL은 빌드가 아니라 실행 시점에 읽는다(tech-stack.md 열린 결정 1·2).
import type { NextRequest } from "next/server";

export const dynamic = "force-dynamic";

const HOP_BY_HOP = ["host", "connection", "content-length", "keep-alive", "transfer-encoding", "upgrade"];

async function proxy(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  const backend = process.env.BACKEND_URL ?? "http://localhost:8080";
  const { path } = await params;
  const target = `${backend}/api/v1/${path.map(encodeURIComponent).join("/")}${request.nextUrl.search}`;

  const headers = new Headers(request.headers);
  HOP_BY_HOP.forEach((name) => headers.delete(name));

  const hasBody = request.method !== "GET" && request.method !== "HEAD";
  let upstream: Response;
  try {
    upstream = await fetch(target, {
      method: request.method,
      headers,
      body: hasBody ? request.body : undefined,
      redirect: "manual",
      cache: "no-store",
      // 요청 본문(사진 업로드 등)을 스트림으로 넘기려면 필요하다.
      ...(hasBody ? { duplex: "half" } : {}),
    } as RequestInit);
  } catch {
    return Response.json({ detail: "서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요." }, { status: 502 });
  }

  const responseHeaders = new Headers(upstream.headers);
  // fetch가 이미 압축을 풀었으므로 원래 인코딩 헤더를 넘기지 않는다.
  ["content-encoding", "content-length", "transfer-encoding", "connection"].forEach((name) =>
    responseHeaders.delete(name),
  );
  return new Response(upstream.body, { status: upstream.status, headers: responseHeaders });
}

export { proxy as GET, proxy as POST, proxy as PUT, proxy as PATCH, proxy as DELETE };
