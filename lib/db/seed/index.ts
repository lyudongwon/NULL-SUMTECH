// 시드 실행기(역할 ①). 각 시드는 여러 번 실행돼도 결과가 같아야 한다.
import type { Db } from "../index";
import { seedFoods } from "./foods";
import { seedPresets } from "./presets";
import { seedRestaurants } from "./restaurants";

export async function runSeed(db: Db) {
  await seedPresets(db); // 제한 항목·프리셋이 먼저 있어야 재료·음식점이 참조할 수 있다
  await seedFoods(db);
  await seedRestaurants(db);
}
