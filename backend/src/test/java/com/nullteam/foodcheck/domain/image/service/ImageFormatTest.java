package com.nullteam.foodcheck.domain.image.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class ImageFormatTest {

	@Test
	void JPEG는_형식과_크기를_읽는다() throws IOException {
		assertEquals(new ImageFormat.Info("image/jpeg", 1400, 1050), ImageFormat.detect(encode("jpg", 1400, 1050)).orElseThrow());
	}

	@Test
	void PNG는_형식과_크기를_읽는다() throws IOException {
		assertEquals(new ImageFormat.Info("image/png", 640, 480), ImageFormat.detect(encode("png", 640, 480)).orElseThrow());
	}

	@Test
	void WEBP_VP8X는_헤더에서_캔버스_크기를_읽는다() {
		byte[] b = webpHeader("VP8X");
		// 캔버스 너비-1, 높이-1 (24비트 리틀 엔디언)
		putU24(b, 24, 1400 - 1);
		putU24(b, 27, 1050 - 1);
		assertEquals(new ImageFormat.Info("image/webp", 1400, 1050), ImageFormat.detect(b).orElseThrow());
	}

	@Test
	void WEBP_VP8L은_비트에서_크기를_읽는다() {
		byte[] b = webpHeader("VP8L");
		b[20] = 0x2F;
		int bits = (800 - 1) | (600 - 1) << 14;
		b[21] = (byte) bits;
		b[22] = (byte) (bits >> 8);
		b[23] = (byte) (bits >> 16);
		b[24] = (byte) (bits >> 24);
		assertEquals(new ImageFormat.Info("image/webp", 800, 600), ImageFormat.detect(b).orElseThrow());
	}

	@Test
	void 이미지가_아니면_empty다() {
		assertTrue(ImageFormat.detect("이건 사진이 아니라 글자입니다".getBytes(StandardCharsets.UTF_8)).isEmpty());
		assertTrue(ImageFormat.detect(new byte[0]).isEmpty());
		assertTrue(ImageFormat.detect(null).isEmpty());
	}

	@Test
	void 머리만_JPEG이고_깨진_파일은_empty다() {
		byte[] b = new byte[64];
		b[0] = (byte) 0xFF;
		b[1] = (byte) 0xD8;
		b[2] = (byte) 0xFF;
		assertTrue(ImageFormat.detect(b).isEmpty());
	}

	private static byte[] encode(String format, int width, int height) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, out);
		return out.toByteArray();
	}

	private static byte[] webpHeader(String chunk) {
		byte[] b = new byte[40];
		System.arraycopy("RIFF".getBytes(StandardCharsets.US_ASCII), 0, b, 0, 4);
		System.arraycopy("WEBP".getBytes(StandardCharsets.US_ASCII), 0, b, 8, 4);
		System.arraycopy(chunk.getBytes(StandardCharsets.US_ASCII), 0, b, 12, 4);
		return b;
	}

	private static void putU24(byte[] b, int i, int value) {
		b[i] = (byte) value;
		b[i + 1] = (byte) (value >> 8);
		b[i + 2] = (byte) (value >> 16);
	}
}
