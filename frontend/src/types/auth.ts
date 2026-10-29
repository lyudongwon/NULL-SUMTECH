// OPT-4 회원가입·로그인 (docs/api.md). 역할 ①이 정한다.

export type AuthUser = { user_id: number; email: string; created_at: string };

export type SignupRequest = { email: string; password: string; user_id?: number };
export type LoginRequest = { email: string; password: string };
