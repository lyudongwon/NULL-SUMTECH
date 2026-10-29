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
# 빌드한 commit을 이미지에 남긴다. 실행할 때 환경변수 RELEASE_ID를 주면 그 값이 우선한다.
ARG RELEASE_ID=dev
ENV NODE_ENV=production PORT=3000 HOSTNAME=0.0.0.0 RELEASE_ID=$RELEASE_ID
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static
COPY --from=build /app/public ./public
EXPOSE 3000
CMD ["node", "server.js"]
