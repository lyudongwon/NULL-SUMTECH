import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Docker 이미지에서 node server.js 하나로 실행하려고 standalone으로 빌드한다.
  output: "standalone",
};

export default nextConfig;
