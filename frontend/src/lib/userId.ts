// 서버가 발급한 user_id를 브라우저에 보관한다. 다른 역할도 이 함수로 읽는다.
const KEY = "user_id";

export function readUserId(): number | null {
  try {
    const value = Number(window.localStorage.getItem(KEY));
    return Number.isInteger(value) && value > 0 ? value : null;
  } catch {
    return null;
  }
}

export function saveUserId(userId: number) {
  try {
    window.localStorage.setItem(KEY, String(userId));
  } catch {
    // 저장소를 못 쓰는 브라우저(시크릿 모드 등)에서는 이번 화면에서만 쓴다.
  }
}

export function clearUserId() {
  try {
    window.localStorage.removeItem(KEY);
  } catch {}
}
