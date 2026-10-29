/**
 * 주최 측 실행·CI·로컬이 같은 이름으로 넘기는 값. 기본값은 docker-compose.yml의 시드 계정(역할 "유학생")과 같다.
 * 주최 측 실행은 docker-compose.yml의 SEED_USER_EMAIL·SEED_USER_PASSWORD를 E2E_USER_EMAIL·E2E_USER_PASSWORD로 넘긴다.
 */
export const E2E_BASE_URL = process.env.E2E_BASE_URL ?? "http://localhost:3000";
export const E2E_USER_EMAIL = process.env.E2E_USER_EMAIL ?? "seed@example.com";
export const E2E_USER_PASSWORD = process.env.E2E_USER_PASSWORD ?? "seed-password-1234";
