package com.coursecraft.ai.lesson.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.model.CourseModule;
import com.coursecraft.ai.course.model.LessonSummary;
import com.coursecraft.ai.course.repository.CourseRepository;
import com.coursecraft.ai.generation.LessonContentGenerator;
import com.coursecraft.ai.lesson.model.LessonContent;
import com.coursecraft.ai.lesson.repository.LessonContentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class LessonServiceTests {

	@Mock
	private CourseRepository courseRepository;

	@Mock
	private LessonContentRepository lessonContentRepository;

	@Mock
	private LessonContentGenerator lessonContentGenerator;

	@InjectMocks
	private LessonService lessonService;

	@Test
	void generateLessonContentReusesExistingContentIdAndMarksLessonGenerated() {
		Course course = course("user-1");
		LessonContent generatedContent = lessonContent(null, "Generated lesson");
		LessonContent existingContent = lessonContent("content-1", "Old lesson");
		LessonContent savedContent = lessonContent("content-1", "Generated lesson");

		when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
		when(lessonContentGenerator.generate(course, course.getModules().get(0), course.getModules().get(0).getLessons().get(0)))
				.thenReturn(generatedContent);
		when(lessonContentRepository.findByCourseIdAndModuleIdAndLessonId("course-1", "module-1", "lesson-1"))
				.thenReturn(Optional.of(existingContent));
		when(lessonContentRepository.save(generatedContent)).thenReturn(savedContent);
		when(courseRepository.save(course)).thenReturn(course);

		var response = lessonService.generateLessonContent("course-1", "module-1", "lesson-1", "user-1");

		assertThat(response.id()).isEqualTo("content-1");
		assertThat(response.title()).isEqualTo("Generated lesson");
		assertThat(course.getModules().get(0).getLessons().get(0).isContentGenerated()).isTrue();
		verify(courseRepository).save(course);
	}

	@Test
	void getLessonContentReturnsExistingContent() {
		Course course = course("user-1");
		when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
		when(lessonContentRepository.findByCourseIdAndModuleIdAndLessonId("course-1", "module-1", "lesson-1"))
				.thenReturn(Optional.of(lessonContent("content-1", "Generated lesson")));

		var response = lessonService.getLessonContent("course-1", "module-1", "lesson-1", "user-1");

		assertThat(response.id()).isEqualTo("content-1");
		assertThat(response.lessonId()).isEqualTo("lesson-1");
	}

	@Test
	void getLessonContentRejectsMissingContent() {
		Course course = course("user-1");
		when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
		when(lessonContentRepository.findByCourseIdAndModuleIdAndLessonId("course-1", "module-1", "lesson-1"))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> lessonService.getLessonContent("course-1", "module-1", "lesson-1", "user-1"))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Lesson content not found");
	}

	@Test
	void rejectsMissingModuleLessonAndForeignCourse() {
		when(courseRepository.findById("course-1")).thenReturn(Optional.of(course("other-user")));

		assertThatThrownBy(() -> lessonService.getLessonContent("course-1", "module-1", "lesson-1", "user-1"))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Course does not belong");

		when(courseRepository.findById("course-2")).thenReturn(Optional.of(course("user-1")));

		assertThatThrownBy(() -> lessonService.getLessonContent("course-2", "missing-module", "lesson-1", "user-1"))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Module not found");

		assertThatThrownBy(() -> lessonService.getLessonContent("course-2", "module-1", "missing-lesson", "user-1"))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Lesson not found");
	}

	private Course course(String creatorId) {
		return Course.builder()
				.id("course-1")
				.title("React Basics")
				.creatorId(creatorId)
				.modules(new ArrayList<>(List.of(CourseModule.builder()
						.id("module-1")
						.title("Module 1")
						.lessons(new ArrayList<>(List.of(LessonSummary.builder()
								.id("lesson-1")
								.title("Lesson 1")
								.build())))
						.build())))
				.build();
	}

	private LessonContent lessonContent(String id, String title) {
		return LessonContent.builder()
				.id(id)
				.courseId("course-1")
				.moduleId("module-1")
				.lessonId("lesson-1")
				.title(title)
				.build();
	}
}
