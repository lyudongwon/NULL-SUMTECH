import { apiRequest } from "@/lib/apiClient";
import type { UserResponse } from "@/types/restriction";

export function createUser() {
  return apiRequest<UserResponse>("/users", { method: "POST" });
}
