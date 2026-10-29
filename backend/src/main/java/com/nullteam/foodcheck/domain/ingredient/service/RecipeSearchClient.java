package com.nullteam.foodcheck.domain.ingredient.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REQ-5 3단계. 만개의 레시피에서 재료 이름을 찾는다. 음식 DB에 없는 메뉴에만 부른다.
 *
 * <p>레시피 상세 페이지의 schema.org JSON-LD(recipeIngredient)를 읽는다. 새 라이브러리를 쓰지 않으려고
 * HTML 파서(Jsoup) 대신 JDK HttpClient와 Jackson만 썼다(의존성은 역할 ①이 고친다, docs/roles.md).
 *
 * <p>robots.txt 확인(2026-10-29): {@code User-agent: *}는 {@code Allow: /}이고 막힌 곳은
 * {@code /admin/ /app/ /static/}이라 {@code /recipe/}는 허용된다. AI 학습 크롤러(GPTBot, ClaudeBot 등)는
 * 따로 막혀 있고 Content-Signal은 {@code ai-train=no, use=reference}다. 학습이 아니라 요청 시점의
 * 참조로만 쓰고, 막힌 크롤러 이름을 쓰지 않도록 User-Agent에 이 프로젝트 이름을 적는다.
 * 사이트 이용약관은 아직 팀이 검토하지 않았다(docs/roles.md 팀 확인 필요). 막아야 하면
 * {@code app.recipe-search.enabled=false}로 끄면 AI 추정(4단계)으로 넘어간다.
 */
@Component
public class RecipeSearchClient {

	private static final Logger log = LoggerFactory.getLogger(RecipeSearchClient.class);

	private static final String SEARCH_URL = "https://www.10000recipe.com/recipe/list.html?q=";
	private static final String RECIPE_URL = "https://www.10000recipe.com";
	private static final String USER_AGENT =
			"NULL-SUMTECH-FoodCheck/0.1 (SUMTECH 2026 hackathon; +https://github.com/lyudongwon/NULL-SUMTECH)";

	private static final Pattern RECIPE_LINK = Pattern.compile("/recipe/(\\d{5,})");
	private static final Pattern LD_JSON = Pattern.compile(
			"<script[^>]*type=\"application/ld\\+json\"[^>]*>(.*?)</script>", Pattern.DOTALL);
	/** "느타리버섯 1줌" 같은 값에서 분량을 떼려고 첫 숫자 앞까지만 쓴다. */
	private static final Pattern QUANTITY = Pattern.compile("[0-9].*$");
	/** "양상추 (곁들일양)" 같은 괄호 설명은 뗀다. */
	private static final Pattern PARENTHESES = Pattern.compile("\\([^)]*\\)");
	/** 숫자가 없는 분량 표현. "소금 간맞추기", "고추 기호에맞게"처럼 뒤에 붙으면 뗀다. */
	private static final Pattern QUANTITY_WORD = Pattern.compile(
			".*(약간|조금|적당량|맞추기|맞게|따라|껏|취향|한줌|두줌|생략|소량|넉넉|살짝|톡톡|기호).*");

	/** 재료 이름으로 보기 어려운 값은 버린다. */
	private static final int MAX_NAME_LENGTH = 20;
	private static final int MAX_INGREDIENTS = 20;

	/** 외부 사이트 HTML을 읽을 때만 쓴다. API 응답용 매퍼(역할 ①의 설정)와 섞지 않으려고 따로 둔다. */
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final HttpClient httpClient;
	private final boolean enabled;

	public RecipeSearchClient(@Value("${app.recipe-search.enabled:true}") boolean enabled) {
		this.enabled = enabled;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(3))
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
	}

	/** 찾지 못하거나 사이트가 막히면 빈 목록을 돌려준다. 그러면 호출한 쪽이 AI 추정으로 넘어간다. */
	public List<String> search(String menuName) {
		if (!enabled) {
			return List.of();
		}
		try {
			String listHtml = get(SEARCH_URL + URLEncoder.encode(menuName, StandardCharsets.UTF_8));
			Matcher link = RECIPE_LINK.matcher(listHtml);
			if (!link.find()) {
				return List.of();
			}
			String detailHtml = get(RECIPE_URL + link.group());
			if (!titleMatches(detailHtml, menuName)) {
				// 검색이 상관없는 레시피를 줄 때가 있다("ㅁㄴㅇㄹ" → 간장조림, "아메리카노" → 베이컨 요리).
				log.info("만개의 레시피 결과가 메뉴와 맞지 않아 쓰지 않는다: menuName={}", menuName);
				return List.of();
			}
			return parseIngredients(detailHtml);
		} catch (Exception e) {
			// 외부 사이트 문제로 판정을 멈추지 않는다. AI 추정으로 넘어간다.
			log.warn("만개의 레시피 검색 실패: menuName={}, cause={}", menuName, e.toString());
			return List.of();
		}
	}

	private String get(String url) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.header("User-Agent", USER_AGENT)
				.header("Accept-Language", "ko-KR,ko")
				.timeout(Duration.ofSeconds(5))
				.GET()
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			throw new IllegalStateException("HTTP " + response.statusCode() + " " + url);
		}
		return response.body();
	}

	/** 레시피 제목(JSON-LD name)에 메뉴 이름이 띄어쓰기 없이 들어 있어야 같은 음식으로 본다. */
	boolean titleMatches(String detailHtml, String menuName) {
		String menu = MenuNameNormalizer.compact(menuName);
		Matcher scripts = LD_JSON.matcher(detailHtml);
		while (scripts.find()) {
			try {
				String title = objectMapper.readTree(scripts.group(1).strip()).path("name").asText("");
				if (!title.isEmpty()) {
					return MenuNameNormalizer.compact(title).contains(menu);
				}
			} catch (Exception e) {
				// 다음 JSON-LD를 본다.
			}
		}
		return false;
	}

	List<String> parseIngredients(String detailHtml) {
		Matcher scripts = LD_JSON.matcher(detailHtml);
		while (scripts.find()) {
			JsonNode ingredients = readRecipeIngredient(scripts.group(1).strip());
			if (ingredients != null && ingredients.isArray()) {
				Set<String> names = new LinkedHashSet<>();
				for (JsonNode node : ingredients) {
					String name = cleanName(node.asText(""));
					if (!name.isEmpty()) {
						names.add(name);
					}
					if (names.size() >= MAX_INGREDIENTS) {
						break;
					}
				}
				if (!names.isEmpty()) {
					return List.copyOf(names);
				}
			}
		}
		return List.of();
	}

	private JsonNode readRecipeIngredient(String json) {
		try {
			return objectMapper.readTree(json).path("recipeIngredient");
		} catch (Exception e) {
			return null;
		}
	}

	/** "고추가루 1.5T" → "고추가루", "소금 간맞추기" → "소금". 이름 같지 않으면 빈 문자열을 돌려준다. */
	static String cleanName(String raw) {
		String name = PARENTHESES.matcher(raw.strip()).replaceAll(" ").strip();
		name = QUANTITY.matcher(name).replaceAll("").strip();
		name = name.replaceAll("\\s+", " ").replaceAll("^[·,/\\-]+|[·,/\\-]+$", "").strip();
		List<String> words = new ArrayList<>(List.of(name.isEmpty() ? new String[0] : name.split(" ")));
		while (words.size() > 1 && QUANTITY_WORD.matcher(words.getLast()).matches()) {
			words.removeLast();
		}
		name = String.join(" ", words);
		// 분량 표현만 남았으면 재료 이름이 아니다.
		if (name.isEmpty() || QUANTITY_WORD.matcher(name).matches() || name.length() > MAX_NAME_LENGTH) {
			return "";
		}
		return name;
	}
}
