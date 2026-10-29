# 화면(Next.js). docker compose의 app 서비스를 이 파일로 빌드한다.
# 제출 요건이 루트 docker-compose.yml과 같은 위치에 Dockerfile을 두라고 해서 frontend/Dockerfile을 여기로 옮겼다.
# 빌드 맥락은 저장소 루트라서 frontend/만 복사한다. 백엔드는 backend/Dockerfile로 따로 빌드한다.
# 빌드는 여기서 끝내고, 컨테이너 시작 명령은 서버 실행만 한다.
FROM node:22-alpine AS build
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM node:22-alpine
WORKDIR /app
ENV NODE_ENV=production PORT=3000 HOSTNAME=0.0.0.0
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static
COPY --from=build /app/public ./public
EXPOSE 3000
CMD ["node", "server.js"]
