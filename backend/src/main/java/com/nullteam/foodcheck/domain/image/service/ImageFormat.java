package com.nullteam.foodcheck.domain.image.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * 사진 바이트의 앞부분으로 형식(JPEG·PNG·WEBP)과 크기를 읽는다. 브라우저가 보낸 Content-Type은 믿지 않는다.
 * WEBP는 JDK ImageIO가 읽지 못해 헤더를 직접 읽는다.
 */
public final class ImageFormat {

	public record Info(String mimeType, int width, int height) {
	}

	private ImageFormat() {
	}

	/** 지원하지 않는 형식이거나 크기를 읽지 못하면 empty. */
	public static Optional<Info> detect(byte[] bytes) {
		if (bytes == null || bytes.length < 12) {
			return Optional.empty();
		}
		if (startsWith(bytes, 0, 0xFF, 0xD8, 0xFF)) {
			return readWithImageIo(bytes, "image/jpeg");
		}
		if (startsWith(bytes, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
			return readWithImageIo(bytes, "image/png");
		}
		if (startsWith(bytes, 0, 'R', 'I', 'F', 'F') && startsWith(bytes, 8, 'W', 'E', 'B', 'P')) {
			return readWebp(bytes);
		}
		return Optional.empty();
	}

	private static Optional<Info> readWithImageIo(byte[] bytes, String mimeType) {
		try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if (!readers.hasNext()) {
				return Optional.empty();
			}
			ImageReader reader = readers.next();
			try {
				reader.setInput(in);
				return Optional.of(new Info(mimeType, reader.getWidth(0), reader.getHeight(0)));
			} finally {
				reader.dispose();
			}
		} catch (IOException | RuntimeException e) {
			return Optional.empty();
		}
	}

	/** RIFF 헤더 뒤 첫 청크(VP8X·VP8·VP8L)에서 캔버스 크기를 읽는다. */
	private static Optional<Info> readWebp(byte[] b) {
		if (b.length < 30) {
			return Optional.empty();
		}
		int width;
		int height;
		if (startsWith(b, 12, 'V', 'P', '8', 'X')) {
			width = 1 + u24(b, 24);
			height = 1 + u24(b, 27);
		} else if (startsWith(b, 12, 'V', 'P', '8', ' ')) {
			if (!startsWith(b, 23, 0x9D, 0x01, 0x2A)) {
				return Optional.empty();
			}
			width = u16(b, 26) & 0x3FFF;
			height = u16(b, 28) & 0x3FFF;
		} else if (startsWith(b, 12, 'V', 'P', '8', 'L')) {
			if ((b[20] & 0xFF) != 0x2F) {
				return Optional.empty();
			}
			int bits = (b[21] & 0xFF) | (b[22] & 0xFF) << 8 | (b[23] & 0xFF) << 16 | (b[24] & 0xFF) << 24;
			width = 1 + (bits & 0x3FFF);
			height = 1 + ((bits >> 14) & 0x3FFF);
		} else {
			return Optional.empty();
		}
		return width > 0 && height > 0 ? Optional.of(new Info("image/webp", width, height)) : Optional.empty();
	}

	private static boolean startsWith(byte[] b, int offset, int... expected) {
		if (b.length < offset + expected.length) {
			return false;
		}
		for (int i = 0; i < expected.length; i++) {
			if ((b[offset + i] & 0xFF) != expected[i]) {
				return false;
			}
		}
		return true;
	}

	private static int u16(byte[] b, int i) {
		return (b[i] & 0xFF) | (b[i + 1] & 0xFF) << 8;
	}

	private static int u24(byte[] b, int i) {
		return (b[i] & 0xFF) | (b[i + 1] & 0xFF) << 8 | (b[i + 2] & 0xFF) << 16;
	}
}
