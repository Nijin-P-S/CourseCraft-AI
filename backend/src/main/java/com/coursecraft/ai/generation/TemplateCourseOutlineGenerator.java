package com.coursecraft.ai.generation;

import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.model.CourseModule;
import com.coursecraft.ai.course.model.LessonSummary;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class TemplateCourseOutlineGenerator implements CourseOutlineGenerator {

	@Override
	public Course generate(String topic, String creatorId) {
		String normalizedTopic = normalizeTopic(topic);

		return Course.builder()
				.title(normalizedTopic + " Learning Path")
				.description("A structured course for learning " + normalizedTopic
						+ " from fundamentals to practical application.")
				.creatorId(creatorId)
				.tags(extractTags(normalizedTopic))
				.modules(buildModules(normalizedTopic))
				.build();
	}

	private List<CourseModule> buildModules(String topic) {
		return List.of(
				module(0, "Foundations of " + topic, "Build the vocabulary and mental models needed to start.", List.of(
						"What is " + topic + "?",
						"Core terminology and concepts",
						"Why " + topic + " matters",
						"Foundation checkpoint"
				)),
				module(1, "Core Concepts", "Understand the main ideas and patterns behind the topic.", List.of(
						"Essential principles",
						"Common workflows",
						"Typical tools and techniques",
						"Concept review quiz"
				)),
				module(2, "Hands-on Practice", "Apply the concepts through practical examples and exercises.", List.of(
						"Guided example",
						"Practice exercise",
						"Common mistakes",
						"Practice checkpoint"
				)),
				module(3, "Next Steps", "Connect the topic to real-world usage and further learning.", List.of(
						"Real-world applications",
						"Best practices",
						"Suggested resources",
						"Final review"
				))
		);
	}

	private CourseModule module(int orderIndex, String title, String description, List<String> lessonTitles) {
		List<LessonSummary> lessons = IntStream.range(0, lessonTitles.size())
				.mapToObj(lessonIndex -> lesson(orderIndex, lessonIndex, lessonTitles.get(lessonIndex)))
				.toList();

		return CourseModule.builder()
				.id(UUID.randomUUID().toString())
				.title(title)
				.description(description)
				.orderIndex(orderIndex)
				.lessons(lessons)
				.build();
	}

	private LessonSummary lesson(int moduleIndex, int lessonIndex, String title) {
		return LessonSummary.builder()
				.id(UUID.randomUUID().toString())
				.title(title)
				.orderIndex((moduleIndex * 100) + lessonIndex)
				.contentGenerated(false)
				.build();
	}

	private String normalizeTopic(String topic) {
		String normalized = topic == null ? "" : topic.trim().replaceAll("\\s+", " ");
		if (normalized.isBlank()) {
			throw new IllegalArgumentException("Topic must not be blank");
		}
		return normalized;
	}

	private List<String> extractTags(String topic) {
		return Stream.of(topic.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
				.filter(token -> !token.isBlank())
				.distinct()
				.limit(5)
				.toList();
	}
}
