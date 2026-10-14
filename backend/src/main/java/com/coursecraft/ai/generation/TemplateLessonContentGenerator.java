package com.coursecraft.ai.generation;

import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.model.CourseModule;
import com.coursecraft.ai.course.model.LessonSummary;
import com.coursecraft.ai.lesson.model.LessonContent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TemplateLessonContentGenerator implements LessonContentGenerator {

	@Override
	public LessonContent generate(Course course, CourseModule module, LessonSummary lesson) {
		return LessonContent.builder()
				.courseId(course.getId())
				.moduleId(module.getId())
				.lessonId(lesson.getId())
				.title(lesson.getTitle())
				.objectives(buildObjectives(course, lesson))
				.contentBlocks(buildContentBlocks(course, module, lesson))
				.metadata(Map.of(
						"generator", "template",
						"videoQuery", course.getTitle() + " " + lesson.getTitle() + " tutorial"
				))
				.build();
	}

	private List<String> buildObjectives(Course course, LessonSummary lesson) {
		return List.of(
				"Explain the purpose of " + lesson.getTitle() + ".",
				"Connect the lesson to the broader course: " + course.getTitle() + ".",
				"Identify key terms and practical use cases.",
				"Answer review questions to check understanding."
		);
	}

	private List<Map<String, Object>> buildContentBlocks(
			Course course,
			CourseModule module,
			LessonSummary lesson
	) {
		return List.of(
				block("heading", Map.of("text", lesson.getTitle())),
				block("paragraph", Map.of(
						"text", "This lesson introduces " + lesson.getTitle()
								+ " as part of the module " + module.getTitle() + "."
				)),
				block("paragraph", Map.of(
						"text", "By studying this topic within " + course.getTitle()
								+ ", you will build a stronger foundation for applying the concept in real scenarios."
				)),
				block("video", Map.of(
						"query", course.getTitle() + " " + lesson.getTitle() + " tutorial"
				)),
				block("mcq", Map.of(
						"question", "What is the main goal of this lesson?",
						"options", List.of(
								"Memorize unrelated facts",
								"Understand and apply the lesson concept",
								"Skip the module overview",
								"Replace hands-on practice"
						),
						"answerIndex", 1,
						"explanation", "The lesson is designed to help learners understand and apply the concept."
				))
		);
	}

	private Map<String, Object> block(String type, Map<String, Object> values) {
		Map<String, Object> block = new LinkedHashMap<>();
		block.put("type", type);
		block.putAll(values);
		return block;
	}
}
