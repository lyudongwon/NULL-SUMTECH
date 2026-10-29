package com.nullteam.foodcheck.domain.menu.service;

/** 사진을 메뉴 이름으로 읽는다. 실제 구현은 SpringAiMenuReader이고, 테스트는 가짜를 넣는다. */
public interface MenuReader {

	MenuReading read(byte[] image, String mimeType);
}
