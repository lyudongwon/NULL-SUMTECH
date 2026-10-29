import path from "node:path";
import { migrate } from "drizzle-orm/node-postgres/migrator";
import { db, pool } from "./index";
import { runSeed } from "./seed";

// 여러 인스턴스가 동시에 떠도 마이그레이션·시드가 한 번씩만 돌도록 잡는 advisory lock 번호.
const SETUP_LOCK_ID = 72_610_001;

export async function migrateAndSeed() {
  if (!process.env.DATABASE_URL) {
    console.warn("[db] DATABASE_URL이 없어 마이그레이션과 시드를 건너뜁니다.");
    return;
  }
  const client = await pool.connect();
  try {
    await client.query("SELECT pg_advisory_lock($1)", [SETUP_LOCK_ID]);
    await migrate(db, { migrationsFolder: path.join(process.cwd(), "drizzle") });
    await runSeed(db);
    console.log("[db] 마이그레이션과 시드 완료");
  } finally {
    await client.query("SELECT pg_advisory_unlock($1)", [SETUP_LOCK_ID]).catch(() => {});
    client.release();
  }
}
