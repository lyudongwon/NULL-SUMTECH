import { apiRequest } from "@/lib/apiClient";
import type {
  IngredientInferResponse,
  JudgmentResponse,
  JudgmentResult,
} from "@/types/judgment";

/** REQ-3 메뉴 여러 개를 한 번에 판정한다. 등록되지 않은 user_id도 404가 아니라 R0으로 돌아온다 */
export function judge(userId: number, menuNames: string[]) {
  return apiRequest<JudgmentResponse>("/judgments", {
    method: "POST",
    body: JSON.stringify({ user_id: userId, menu_names: menuNames }),
  });
}

/** OPT-1 직접 입력한 메뉴 하나를 판정한다. 음식 DB에 없는 메뉴도 팝업 없이 판정된다 */
export function judgeManual(userId: number, menuName: string) {
  return apiRequest<JudgmentResult>("/judgments/manual", {
    method: "POST",
    body: JSON.stringify({ user_id: userId, menu_name: menuName }),
  });
}

/** REQ-5 재료 찾기를 직접 확인할 때 쓴다. 판정은 서버 안에서 같은 함수를 부른다 */
export function inferIngredients(menuName: string) {
  return apiRequest<IngredientInferResponse>("/ingredients/infer", {
    method: "POST",
    body: JSON.stringify({ menu_name: menuName }),
  });
}
