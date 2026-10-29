// user_id 발급 흐름. 브라우저에 없으면 서버에서 새로 발급받아 보관한다.
import { readUserId, saveUserId } from "@/lib/userId";
import { createUser } from "@/services/userService";

export async function ensureUserId(): Promise<number> {
  const saved = readUserId();
  if (saved) return saved;
  const { user_id } = await createUser();
  saveUserId(user_id);
  return user_id;
}
