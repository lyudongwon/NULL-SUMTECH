package com.nullteam.foodcheck.domain.restaurant.entity;

import java.util.Set;
import java.util.TreeSet;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "restaurants")
public class Restaurant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String name;

	private String category;

	private String address;

	/** OPT-2 지도(V2). 카카오맵 장소 id·영어 이름·좌표. 좌표가 없는 음식점은 지도에 나오지 않는다. */
	@Column(name = "kakao_place_id")
	private String kakaoPlaceId;

	@Column(name = "name_en")
	private String nameEn;

	private Double lat;

	private Double lng;

	/** KAKAO(카카오 호출로 쌓임), SEED(시연용 시드). */
	@Column(nullable = false)
	private String source = "SEED";

	/** 옛 추천 목록용. 이 음식점이 충족하는(쓰지 않는) 제한 항목 코드. 옛 화면·흐름 S8을 바꾼 뒤 V 파일과 함께 지운다. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "restaurant_restriction_items", joinColumns = @JoinColumn(name = "restaurant_id"))
	@Column(name = "item_code")
	private Set<String> satisfiedItems = new TreeSet<>();

	protected Restaurant() {
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public String getAddress() {
		return address;
	}

	public String getKakaoPlaceId() {
		return kakaoPlaceId;
	}

	public String getNameEn() {
		return nameEn;
	}

	public Double getLat() {
		return lat;
	}

	public Double getLng() {
		return lng;
	}

	public String getSource() {
		return source;
	}

	public Set<String> getSatisfiedItems() {
		return satisfiedItems;
	}
}
