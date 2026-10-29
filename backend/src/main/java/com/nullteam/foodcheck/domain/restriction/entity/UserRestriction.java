package com.nullteam.foodcheck.domain.restriction.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 사용자당 한 행. 마지막으로 고른 프리셋과 낙관적 락용 version. 쓰기는 repository의 조건부 쿼리로만 한다. */
@Entity
@Table(name = "user_restrictions")
public class UserRestriction {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "preset_code")
	private String presetCode;

	@Column(nullable = false)
	private int version;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected UserRestriction() {
	}

	public Long getUserId() {
		return userId;
	}

	public String getPresetCode() {
		return presetCode;
	}

	public int getVersion() {
		return version;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
