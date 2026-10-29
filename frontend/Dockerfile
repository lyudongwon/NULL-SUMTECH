# 화면(Next.js). docker compose의 app 서비스로 3000 포트에서 받고, /api/v1/**은 BACKEND_URL로 넘긴다.
# 빌드는 여기서 끝내고, 컨테이너 시작 명령은 서버 실행만 한다.
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:22-alpine
WORKDIR /app
ENV NODE_ENV=production PORT=3000 HOSTNAME=0.0.0.0
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static
COPY --from=build /app/public ./public
EXPOSE 3000
CMD ["node", "server.js"]
