// REQ-4 식단 제한 (docs/api.md). 역할 ①이 정한다.

export type PresetCode = "HALAL" | "VEGETARIAN" | "VEGAN";

export type Preset = { code: PresetCode; name: string; items: string[] };
export type RestrictionItem = { code: string; name: string };

export type PresetListResponse = { presets: Preset[]; items: RestrictionItem[] };

export type Toggles = Record<string, boolean>;

export type RestrictionResponse = {
  user_id: number;
  preset: PresetCode | null;
  toggles: Toggles;
  version: number;
  updated_at: string;
};

export type RestrictionPatchRequest = {
  preset?: PresetCode | null;
  toggles?: Toggles;
  version?: number;
};

export type UserResponse = { user_id: number; created_at: string };
