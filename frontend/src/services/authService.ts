import { apiRequest } from "@/lib/apiClient";
import type { AuthUser, LoginRequest, SignupRequest } from "@/types/auth";

export function signup(body: SignupRequest) {
  return apiRequest<AuthUser>("/auth/signup", { method: "POST", body: JSON.stringify(body) });
}

export function login(body: LoginRequest) {
  return apiRequest<AuthUser>("/auth/login", { method: "POST", body: JSON.stringify(body) });
}

export function logout() {
  return apiRequest<null>("/auth/logout", { method: "POST" });
}

/** 로그인하지 않았으면 401(ApiError)이다. */
export function me() {
  return apiRequest<AuthUser>("/auth/me");
}
