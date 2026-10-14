package com.coursecraft.ai.generation;

import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.model.CourseModule;
import com.coursecraft.ai.course.model.LessonSummary;
import com.coursecraft.ai.lesson.model.LessonContent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiLessonContentGenerator implements LessonContentGenerator {

	private static final TypeReference<LinkedHashMap<String, Object>> CONTENT_BLOCK_TYPE = new TypeReference<>() {
	};

	private final ObjectMapper objectMapper;
	private final CoursePromptBuilder coursePromptBuilder;
	private final RestClient restClient;
	private final String apiKey;
	private final String model;

	public GeminiLessonContentGenerator(
			CoursePromptBuilder coursePromptBuilder,
			@Value("${app.ai.gemini.api-key}") String apiKey,
			@Value("${app.ai.gemini.model}") String model
	) {
		this.objectMapper = new ObjectMapper();
		this.coursePromptBuilder = coursePromptBuilder;
		this.restClient = RestClient.create("https://generativelanguage.googleapis.com");
		this.apiKey = apiKey;
		this.model = model;
	}

	@Override
	public LessonContent generate(Course course, CourseModule module, LessonSummary lesson) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException("Gemini API key is required when AI_PROVIDER=gemini");
		}

		String prompt = coursePromptBuilder.buildLessonContentPrompt(
				course.getTitle(),
				course.getDescription(),
				module.getTitle(),
				module.getDescription(),
				lesson.getTitle()
		);
		String responseBody = restClient.post()
				.uri("/v1beta/models/{model}:generateContent?key={apiKey}", model, apiKey)
				.body(buildRequest(prompt))
				.retrieve()
				.body(String.class);

		return toLessonContent(parseLessonJson(responseBody), course, module, lesson);
	}

	private Map<String, Object> buildRequest(String prompt) {
		Map<String, Object> part = Map.of("text", prompt);
		Map<String, Object> content = Map.of("parts", List.of(part));
		Map<String, Object> generationConfig = Map.of("responseMimeType", "application/json");

		return Map.of(
				"contents", List.of(content),
				"generationConfig", generationConfig
		);
	}

	private JsonNode parseLessonJson(String responseBody) {
		try {
			JsonNode root = objectMapper.readTree(responseBody);
			String text = root.path("candidates")
					.path(0)
					.path("content")
					.path("parts")
					.path(0)
					.path("text")
					.asText();

			if (text.isBlank()) {
				throw new IllegalStateException("Gemini response did not contain generated lesson JSON");
			}

			return objectMapper.readTree(stripCodeFence(text));
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to parse Gemini lesson generation response", exception);
		}
	}

	private LessonContent toLessonContent(
			JsonNode lessonJson,
			Course course,
			CourseModule module,
			LessonSummary lesson
	) {
		List<Map<String, Object>> contentBlocks = readContentBlocks(lessonJson.path("contentBlocks"));
		String videoQuery = findVideoQuery(contentBlocks, course, lesson);

		return LessonContent.builder()
				.courseId(course.getId())
				.moduleId(module.getId())
				.lessonId(lesson.getId())
				.title(requiredText(lessonJson, "title"))
				.objectives(readStringArray(lessonJson.path("objectives")))
				.contentBlocks(contentBlocks)
				.metadata(Map.of(
						"generator", "gemini",
						"videoQuery", videoQuery
				))
				.build();
	}

	private List<Map<String, Object>> readContentBlocks(JsonNode contentBlocksNode) {
		if (!contentBlocksNode.isArray() || contentBlocksNode.isEmpty()) {
			throw new IllegalStateException("Generated lesson must include contentBlocks");
		}

		List<Map<String, Object>> contentBlocks = new ArrayList<>();
		contentBlocksNode.forEach(blockNode -> {
			Map<String, Object> block = objectMapper.convertValue(blockNode, CONTENT_BLOCK_TYPE);
			if (!(block.get("type") instanceof String type) || type.isBlank()) {
				throw new IllegalStateException("Generated lesson content block is missing type");
			}
			contentBlocks.add(block);
		});

		return contentBlocks;
	}

	private List<String> readStringArray(JsonNode arrayNode) {
		if (!arrayNode.isArray() || arrayNode.isEmpty()) {
			throw new IllegalStateException("Generated lesson must include objectives");
		}

		List<String> values = new ArrayList<>();
		arrayNode.forEach(value -> {
			if (!value.asText().isBlank()) {
				values.add(value.asText());
			}
		});

		if (values.isEmpty()) {
			throw new IllegalStateException("Generated lesson must include non-empty objectives");
		}

		return values;
	}

	private String findVideoQuery(List<Map<String, Object>> contentBlocks, Course course, LessonSummary lesson) {
		return contentBlocks.stream()
				.filter(block -> "video".equals(block.get("type")))
				.map(block -> block.get("query"))
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.filter(query -> !query.isBlank())
				.findFirst()
				.orElse(course.getTitle() + " " + lesson.getTitle() + " tutorial");
	}

	private String requiredText(JsonNode node, String fieldName) {
		String value = node.path(fieldName).asText();
		if (value.isBlank()) {
			throw new IllegalStateException("Generated lesson is missing field: " + fieldName);
		}
		return value;
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
