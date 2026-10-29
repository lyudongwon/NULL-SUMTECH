CREATE TABLE "food_ingredients" (
	"food_id" integer NOT NULL,
	"ingredient_id" integer NOT NULL,
	"basis" text,
	"confirmed" boolean DEFAULT false NOT NULL,
	"confidence" text,
	CONSTRAINT "food_ingredients_food_id_ingredient_id_pk" PRIMARY KEY("food_id","ingredient_id")
);
--> statement-breakpoint
CREATE TABLE "foods" (
	"id" serial PRIMARY KEY NOT NULL,
	"name" text NOT NULL,
	"aliases" text[] DEFAULT '{}' NOT NULL,
	"source" text DEFAULT 'seed' NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	CONSTRAINT "foods_name_unique" UNIQUE("name")
);
--> statement-breakpoint
CREATE TABLE "images" (
	"id" text PRIMARY KEY NOT NULL,
	"data" "bytea" NOT NULL,
	"mime_type" text NOT NULL,
	"width" integer,
	"height" integer,
	"sha256" text,
	"source" text,
	"uploaded_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "ingredients" (
	"id" serial PRIMARY KEY NOT NULL,
	"name" text NOT NULL,
	"restriction_code" text,
	CONSTRAINT "ingredients_name_unique" UNIQUE("name")
);
--> statement-breakpoint
CREATE TABLE "presets" (
	"code" text PRIMARY KEY NOT NULL,
	"name" text NOT NULL,
	"items" text[] NOT NULL,
	"sort_order" integer DEFAULT 0 NOT NULL
);
--> statement-breakpoint
CREATE TABLE "restaurants" (
	"id" serial PRIMARY KEY NOT NULL,
	"name" text NOT NULL,
	"category" text,
	"address" text,
	"excludes" text[] DEFAULT '{}' NOT NULL
);
--> statement-breakpoint
CREATE TABLE "restriction_items" (
	"code" text PRIMARY KEY NOT NULL,
	"name" text NOT NULL,
	"sort_order" integer DEFAULT 0 NOT NULL
);
--> statement-breakpoint
CREATE TABLE "user_restrictions" (
	"user_id" uuid PRIMARY KEY NOT NULL,
	"preset" text,
	"toggles" jsonb DEFAULT '{}'::jsonb NOT NULL,
	"version" integer DEFAULT 1 NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "users" (
	"id" uuid PRIMARY KEY NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
ALTER TABLE "food_ingredients" ADD CONSTRAINT "food_ingredients_food_id_foods_id_fk" FOREIGN KEY ("food_id") REFERENCES "public"."foods"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "food_ingredients" ADD CONSTRAINT "food_ingredients_ingredient_id_ingredients_id_fk" FOREIGN KEY ("ingredient_id") REFERENCES "public"."ingredients"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "ingredients" ADD CONSTRAINT "ingredients_restriction_code_restriction_items_code_fk" FOREIGN KEY ("restriction_code") REFERENCES "public"."restriction_items"("code") ON DELETE no action ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "user_restrictions" ADD CONSTRAINT "user_restrictions_user_id_users_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "user_restrictions" ADD CONSTRAINT "user_restrictions_preset_presets_code_fk" FOREIGN KEY ("preset") REFERENCES "public"."presets"("code") ON DELETE no action ON UPDATE no action;