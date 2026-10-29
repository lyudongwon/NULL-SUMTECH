// 서버가 시작될 때 한 번 실행된다. 빈 DB에서도 표 생성과 시드가 끝난 뒤 요청을 받도록 여기서 처리한다.
export async function register() {
  if (process.env.NEXT_RUNTIME !== "nodejs") return;
  const { migrateAndSeed } = await import("./lib/db/setup");
  try {
    await migrateAndSeed();
  } catch (error) {
    // DB가 잠깐 안 떠 있어도 /__health는 응답하도록 서버는 계속 띄우고 오류만 남긴다.
    console.error("[db] 마이그레이션·시드 실패", error);
  }
}
