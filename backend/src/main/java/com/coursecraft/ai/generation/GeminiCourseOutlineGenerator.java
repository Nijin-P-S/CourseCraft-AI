package com.coursecraft.ai.generation;

import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.model.CourseModule;
import com.coursecraft.ai.course.model.LessonSummary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiCourseOutlineGenerator implements CourseOutlineGenerator {

	private final ObjectMapper objectMapper;
	private final CoursePromptBuilder coursePromptBuilder;
	private final RestClient restClient;
	private final String apiKey;
	private final String model;

	public GeminiCourseOutlineGenerator(
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
	public Course generate(String topic, String creatorId) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException("Gemini API key is required when AI_PROVIDER=gemini");
		}

		String prompt = coursePromptBuilder.buildCourseOutlinePrompt(topic);
		String responseBody = restClient.post()
				.uri("/v1beta/models/{model}:generateContent?key={apiKey}", model, apiKey)
				.body(buildRequest(prompt))
				.retrieve()
				.body(String.class);

		return toCourse(parseCourseJson(responseBody), creatorId);
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

	private JsonNode parseCourseJson(String responseBody) {
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
				throw new IllegalStateException("Gemini response did not contain generated course JSON");
			}

			return objectMapper.readTree(stripCodeFence(text));
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to parse Gemini course generation response", exception);
		}
	}

	private Course toCourse(JsonNode courseJson, String creatorId) {
		return Course.builder()
				.title(requiredText(courseJson, "title"))
				.description(requiredText(courseJson, "description"))
				.creatorId(creatorId)
				.tags(readStringArray(courseJson.path("tags")))
				.modules(readModules(courseJson.path("modules")))
				.build();
	}

	private List<CourseModule> readModules(JsonNode modulesNode) {
		if (!modulesNode.isArray() || modulesNode.isEmpty()) {
			throw new IllegalStateException("Generated course must include modules");
		}

		List<CourseModule> modules = new ArrayList<>();
		IntStream.range(0, modulesNode.size()).forEach(moduleIndex -> {
			JsonNode moduleNode = modulesNode.get(moduleIndex);
			modules.add(CourseModule.builder()
					.id(UUID.randomUUID().toString())
					.title(requiredText(moduleNode, "title"))
					.description(moduleNode.path("description").asText(""))
					.orderIndex(moduleIndex)
					.lessons(readLessons(moduleNode.path("lessons"), moduleIndex))
					.build());
		});

		return modules;
	}

	private List<LessonSummary> readLessons(JsonNode lessonsNode, int moduleIndex) {
		if (!lessonsNode.isArray() || lessonsNode.isEmpty()) {
			throw new IllegalStateException("Generated module must include lessons");
		}

		List<LessonSummary> lessons = new ArrayList<>();
		IntStream.range(0, lessonsNode.size()).forEach(lessonIndex -> lessons.add(LessonSummary.builder()
				.id(UUID.randomUUID().toString())
				.title(lessonsNode.get(lessonIndex).asText())
				.orderIndex((moduleIndex * 100) + lessonIndex)
				.contentGenerated(false)
				.build()));

		return lessons;
	}

	private List<String> readStringArray(JsonNode arrayNode) {
		if (!arrayNode.isArray()) {
			return List.of();
		}

		List<String> values = new ArrayList<>();
		arrayNode.forEach(value -> {
			if (!value.asText().isBlank()) {
				values.add(value.asText());
			}
		});

		return values;
	}

	private String requiredText(JsonNode node, String fieldName) {
		String value = node.path(fieldName).asText();
		if (value.isBlank()) {
			throw new IllegalStateException("Generated course is missing field: " + fieldName);
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
