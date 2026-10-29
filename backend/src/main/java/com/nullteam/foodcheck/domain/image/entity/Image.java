package com.nullteam.foodcheck.domain.image.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 업로드한 사진의 메타데이터. 사진 본문은 ImageBodyStore에 둔다(docs/api.md > REQ-1 > 사진 본문 저장). */
@Entity
@Table(name = "images")
public class Image {

	@Id
	private String id;

	private Integer width;

	private Integer height;

	private String source;

	@Column(name = "mime_type")
	private String mimeType;

	@Column(name = "uploaded_at", nullable = false)
	private OffsetDateTime uploadedAt;

	protected Image() {
	}

	public static Image create(String id, int width, int height, String source, String mimeType) {
		Image image = new Image();
		image.id = id;
		image.width = width;
		image.height = height;
		image.source = source;
		image.mimeType = mimeType;
		image.uploadedAt = OffsetDateTime.now();
		return image;
	}

	public String getId() {
		return id;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	public String getSource() {
		return source;
	}

	public String getMimeType() {
		return mimeType;
	}

	public OffsetDateTime getUploadedAt() {
		return uploadedAt;
	}
}
