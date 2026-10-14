package com.coursecraft.ai.lesson.service;

import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.model.CourseModule;
import com.coursecraft.ai.course.model.LessonSummary;
import com.coursecraft.ai.course.repository.CourseRepository;
import com.coursecraft.ai.generation.LessonContentGenerator;
import com.coursecraft.ai.lesson.dto.LessonContentResponse;
import com.coursecraft.ai.lesson.model.LessonContent;
import com.coursecraft.ai.lesson.repository.LessonContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LessonService {

	private final CourseRepository courseRepository;
	private final LessonContentRepository lessonContentRepository;
	private final LessonContentGenerator lessonContentGenerator;

	public LessonContentResponse generateLessonContent(
			String courseId,
			String moduleId,
			String lessonId,
			String userId
	) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

		if (!course.getCreatorId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course does not belong to the authenticated user");
		}

		CourseModule module = findModule(course, moduleId);
		LessonSummary lesson = findLesson(module, lessonId);

		LessonContent generatedContent = lessonContentGenerator.generate(course, module, lesson);
		lessonContentRepository.findByCourseIdAndModuleIdAndLessonId(courseId, moduleId, lessonId)
				.ifPresent(existingContent -> generatedContent.setId(existingContent.getId()));

		LessonContent savedContent = lessonContentRepository.save(generatedContent);
		lesson.setContentGenerated(true);
		courseRepository.save(course);

		return LessonContentResponse.from(savedContent);
	}

	private CourseModule findModule(Course course, String moduleId) {
		return course.getModules().stream()
				.filter(module -> module.getId().equals(moduleId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
	}

	private LessonSummary findLesson(CourseModule module, String lessonId) {
		return module.getLessons().stream()
				.filter(lesson -> lesson.getId().equals(lessonId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
	}
}
