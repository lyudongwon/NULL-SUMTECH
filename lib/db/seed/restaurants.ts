// OPT-2 음식점 시드.
// TODO(확인): 실제 음식점 데이터 출처가 정해지지 않아 이름에 "(예시)"를 붙인 가짜 데이터만 넣는다.
import { sql } from "drizzle-orm";
import type { Db } from "../index";
import { restaurants } from "../schema";

const SAMPLE_RESTAURANTS = [
  { name: "(예시) 할랄 케밥", category: "중동식", address: "TODO(확인)", excludes: ["pork", "alcohol"] },
  { name: "(예시) 비건 샐러드", category: "샐러드", address: "TODO(확인)", excludes: ["meat", "pork", "beef", "seafood", "egg", "dairy"] },
  { name: "(예시) 채식 비빔밥", category: "한식", address: "TODO(확인)", excludes: ["meat", "pork", "beef", "seafood"] },
  { name: "(예시) 인도 커리", category: "인도식", address: "TODO(확인)", excludes: ["pork", "beef", "alcohol"] },
  { name: "(예시) 분식집", category: "분식", address: "TODO(확인)", excludes: [] },
];

export async function seedRestaurants(db: Db) {
  const [{ count }] = await db.select({ count: sql<number>`count(*)::int` }).from(restaurants);
  if (count > 0) return;
  await db.insert(restaurants).values(SAMPLE_RESTAURANTS);
}
