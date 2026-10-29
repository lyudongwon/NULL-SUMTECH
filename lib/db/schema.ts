// DB 표 정의. 이 파일은 역할 ①만 고친다(docs/roles.md). 바꾸면 `npm run db:generate`로 마이그레이션을 만든다.
import {
  boolean,
  customType,
  integer,
  jsonb,
  pgTable,
  primaryKey,
  serial,
  text,
  timestamp,
  uuid,
} from "drizzle-orm/pg-core";

const bytea = customType<{ data: Buffer }>({
  dataType() {
    return "bytea";
  },
});

// ---- REQ-4 식단 제한 ----

export const users = pgTable("users", {
  id: uuid("id").primaryKey(), // 클라이언트가 만든 UUID(user_id)
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
});

// 토글 항목(pork, beef, seafood …). 판정 엔진의 ingredients.restriction_code도 이 코드를 쓴다.
export const restrictionItems = pgTable("restriction_items", {
  code: text("code").primaryKey(),
  name: text("name").notNull(),
  sortOrder: integer("sort_order").notNull().default(0),
});

export const presets = pgTable("presets", {
  code: text("code").primaryKey(), // HALAL, VEGAN
  name: text("name").notNull(),
  items: text("items").array().notNull(), // 켜지는 restriction_items.code
  sortOrder: integer("sort_order").notNull().default(0),
});

export const userRestrictions = pgTable("user_restrictions", {
  userId: uuid("user_id")
    .primaryKey()
    .references(() => users.id, { onDelete: "cascade" }),
  preset: text("preset").references(() => presets.code),
  toggles: jsonb("toggles").$type<Record<string, boolean>>().notNull().default({}),
  version: integer("version").notNull().default(1), // 낙관적 잠금(동시 요청 → 409)
  updatedAt: timestamp("updated_at", { withTimezone: true }).notNull().defaultNow(),
});

// ---- REQ-1·REQ-2 사진 (역할 ②가 사용) ----

export const images = pgTable("images", {
  id: text("id").primaryKey(), // img_xxxxxx
  data: bytea("data").notNull(),
  mimeType: text("mime_type").notNull(),
  width: integer("width"),
  height: integer("height"),
  sha256: text("sha256"),
  source: text("source"), // FILE · DRAG_DROP · CAMERA
  uploadedAt: timestamp("uploaded_at", { withTimezone: true }).notNull().defaultNow(),
});

// ---- REQ-5·REQ-3 음식·재료 (역할 ③이 사용) ----

export const foods = pgTable("foods", {
  id: serial("id").primaryKey(),
  name: text("name").notNull().unique(),
  aliases: text("aliases").array().notNull().default([]),
  source: text("source").notNull().default("seed"), // seed · recipe_search
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
});

export const ingredients = pgTable("ingredients", {
  id: serial("id").primaryKey(),
  name: text("name").notNull().unique(),
  restrictionCode: text("restriction_code").references(() => restrictionItems.code), // 걸리는 제한 항목, 없으면 null
});

export const foodIngredients = pgTable(
  "food_ingredients",
  {
    foodId: integer("food_id")
      .notNull()
      .references(() => foods.id, { onDelete: "cascade" }),
    ingredientId: integer("ingredient_id")
      .notNull()
      .references(() => ingredients.id, { onDelete: "cascade" }),
    basis: text("basis"), // NAME_KEYWORD · COOKING_METHOD 등
    confirmed: boolean("confirmed").notNull().default(false), // 확인(true) / 추정(false)
    confidence: text("confidence"), // high · medium · low
  },
  (t) => [primaryKey({ columns: [t.foodId, t.ingredientId] })],
);

// ---- OPT-2 음식점 추천 ----

export const restaurants = pgTable("restaurants", {
  id: serial("id").primaryKey(),
  name: text("name").notNull(),
  category: text("category"),
  address: text("address"),
  excludes: text("excludes").array().notNull().default([]), // 이 식당이 쓰지 않는 restriction_items.code
});
