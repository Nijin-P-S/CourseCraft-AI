package com.coursecraft.ai.progress.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.model.CourseModule;
import com.coursecraft.ai.course.model.LessonSummary;
import com.coursecraft.ai.course.repository.CourseRepository;
import com.coursecraft.ai.progress.dto.UpdateLessonProgressRequest;
import com.coursecraft.ai.progress.model.CourseProgress;
import com.coursecraft.ai.progress.repository.CourseProgressRepository;
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
class CourseProgressServiceTests {

	@Mock
	private CourseRepository courseRepository;

	@Mock
	private CourseProgressRepository courseProgressRepository;

	@InjectMocks
	private CourseProgressService courseProgressService;

	@Test
	void getProgressCreatesProgressWhenNoneExists() {
		when(courseRepository.findById("course-1")).thenReturn(Optional.of(course("user-1")));
		when(courseProgressRepository.findByUserIdAndCourseId("user-1", "course-1"))
				.thenReturn(Optional.empty());
		when(courseProgressRepository.save(any(CourseProgress.class)))
				.thenAnswer(invocation -> {
					CourseProgress progress = invocation.getArgument(0);
					progress.setId("progress-1");
					return progress;
				});

		var response = courseProgressService.getProgress("course-1", "user-1");

		assertThat(response.id()).isEqualTo("progress-1");
		assertThat(response.userId()).isEqualTo("user-1");
		assertThat(response.courseId()).isEqualTo("course-1");
	}

	@Test
	void updateLessonProgressTracksCompletionAndScore() {
		CourseProgress progress = CourseProgress.builder()
				.id("progress-1")
				.userId("user-1")
				.courseId("course-1")
				.build();

		when(courseRepository.findById("course-1")).thenReturn(Optional.of(course("user-1")));
		when(courseProgressRepository.findByUserIdAndCourseId("user-1", "course-1"))
				.thenReturn(Optional.of(progress));
		when(courseProgressRepository.save(progress)).thenReturn(progress);

		var response = courseProgressService.updateLessonProgress(
				"course-1",
				"lesson-1",
				"user-1",
				new UpdateLessonProgressRequest(true, 85)
		);

		assertThat(response.lastAccessedLessonId()).isEqualTo("lesson-1");
		assertThat(response.completedLessonIds()).containsExactly("lesson-1");
		assertThat(response.assessmentScores()).containsEntry("lesson-1", 85);
	}

	@Test
	void updateLessonProgressRejectsUnknownLesson() {
		when(courseRepository.findById("course-1")).thenReturn(Optional.of(course("user-1")));

		assertThatThrownBy(() -> courseProgressService.updateLessonProgress(
				"course-1",
				"missing-lesson",
				"user-1",
				new UpdateLessonProgressRequest(true, null)
		))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Lesson not found");
	}

	@Test
	void rejectsMissingOrForeignCourses() {
		when(courseRepository.findById("missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> courseProgressService.getProgress("missing", "user-1"))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Course not found");

		when(courseRepository.findById("course-1")).thenReturn(Optional.of(course("other-user")));

		assertThatThrownBy(() -> courseProgressService.getProgress("course-1", "user-1"))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Course does not belong");
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
}
