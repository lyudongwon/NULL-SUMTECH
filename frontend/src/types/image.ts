// REQ-1 사진 업로드 (docs/api.md). 역할 ②가 정한다.

export type ImageSource = "FILE" | "DRAG_DROP" | "CAMERA";

export type ImageUploadResponse = {
  image_id: string;
  width: number;
  height: number;
  uploaded_at: string;
};
