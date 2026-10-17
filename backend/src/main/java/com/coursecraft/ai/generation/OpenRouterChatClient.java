package com.coursecraft.ai.generation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openrouter")
public class OpenRouterChatClient {

	private final ObjectMapper objectMapper;
	private final RestClient restClient;
	private final String apiKey;
	private final String model;
	private final String referer;
	private final String appTitle;

	public OpenRouterChatClient(
			@Value("${app.ai.openrouter.base-url}") String baseUrl,
			@Value("${app.ai.openrouter.api-key}") String apiKey,
			@Value("${app.ai.openrouter.model}") String model,
			@Value("${app.ai.openrouter.referer}") String referer,
			@Value("${app.ai.openrouter.title}") String appTitle
	) {
		this.objectMapper = new ObjectMapper();
		this.restClient = RestClient.create(baseUrl);
		this.apiKey = apiKey;
		this.model = model;
		this.referer = referer;
		this.appTitle = appTitle;
	}

	public JsonNode completeJson(String prompt) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException("OpenRouter API key is required when AI_PROVIDER=openrouter");
		}

		String responseBody = restClient.post()
				.uri("/chat/completions")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.header("HTTP-Referer", referer)
				.header("X-Title", appTitle)
				.body(buildRequest(prompt))
				.retrieve()
				.body(String.class);

		return parseJsonContent(responseBody);
	}

	private Map<String, Object> buildRequest(String prompt) {
		return Map.of(
				"model", model,
				"messages", List.of(
						Map.of(
								"role", "system",
								"content", "You generate valid raw JSON only for CourseCraft AI."
						),
						Map.of(
								"role", "user",
								"content", prompt
						)
				),
				"temperature", 0.3
		);
	}

	private JsonNode parseJsonContent(String responseBody) {
		try {
			JsonNode root = objectMapper.readTree(responseBody);
			String text = root.path("choices")
					.path(0)
					.path("message")
					.path("content")
					.asText();

			if (text.isBlank()) {
				throw new IllegalStateException("OpenRouter response did not contain generated JSON");
			}

			return objectMapper.readTree(stripCodeFence(text));
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to parse OpenRouter generation response", exception);
		}
	}

	private String stripCodeFence(String text) {
		String trimmed = text.trim();
		if (!trimmed.startsWith("```")) {
			return trimmed;
		}

		return trimmed
				.replaceFirst("^```(?:json)?\\s*", "")
				.replaceFirst("\\s*```$", "")
				.trim();
	}
}
