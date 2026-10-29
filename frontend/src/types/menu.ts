// REQ-2 메뉴 판독 (docs/api.md). 역할 ②가 정한다.

export type ImageType = "MENU_BOARD" | "FOOD";

/** 읽지 못한 메뉴는 name이 null이다. */
export type RecognizedMenu = { index: number; name: string | null };

export type MenuRecognizeRequest = { image_id: string };

export type MenuRecognizeResponse = {
  image_id: string;
  image_type: ImageType;
  menus: RecognizedMenu[];
};
