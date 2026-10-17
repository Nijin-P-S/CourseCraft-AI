package com.coursecraft.ai.generation;

import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.model.CourseModule;
import com.coursecraft.ai.course.model.LessonSummary;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openrouter")
public class OpenRouterCourseOutlineGenerator implements CourseOutlineGenerator {

	private final CoursePromptBuilder coursePromptBuilder;
	private final OpenRouterChatClient openRouterChatClient;

	public OpenRouterCourseOutlineGenerator(
			CoursePromptBuilder coursePromptBuilder,
			OpenRouterChatClient openRouterChatClient
	) {
		this.coursePromptBuilder = coursePromptBuilder;
		this.openRouterChatClient = openRouterChatClient;
	}

	@Override
	public Course generate(String topic, String creatorId) {
		JsonNode courseJson = openRouterChatClient.completeJson(
				coursePromptBuilder.buildCourseOutlinePrompt(topic)
		);
		return toCourse(courseJson, creatorId);
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
}
