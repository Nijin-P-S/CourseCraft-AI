package com.coursecraft.ai.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coursecraft.ai.course.dto.GenerateCourseRequest;
import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.repository.CourseRepository;
import com.coursecraft.ai.generation.CourseOutlineGenerator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CourseServiceTests {

	@Mock
	private CourseRepository courseRepository;

	@Mock
	private CourseOutlineGenerator courseOutlineGenerator;

	@InjectMocks
	private CourseService courseService;

	@Test
	void generateCourseSavesGeneratedOutline() {
		Course generatedCourse = Course.builder()
				.title("React Basics")
				.description("Learn React")
				.creatorId("user-1")
				.build();
		Course savedCourse = Course.builder()
				.id("course-1")
				.title("React Basics")
				.description("Learn React")
				.creatorId("user-1")
				.build();

		when(courseOutlineGenerator.generate("React", "user-1")).thenReturn(generatedCourse);
		when(courseRepository.save(generatedCourse)).thenReturn(savedCourse);

		var response = courseService.generateCourse(new GenerateCourseRequest("React"), "user-1");

		assertThat(response.id()).isEqualTo("course-1");
		assertThat(response.title()).isEqualTo("React Basics");
		verify(courseRepository).save(generatedCourse);
	}

	@Test
	void getCoursesForUserReturnsMappedCourses() {
		when(courseRepository.findByCreatorIdOrderByCreatedAtDesc("user-1"))
				.thenReturn(List.of(Course.builder()
						.id("course-1")
						.title("React Basics")
						.creatorId("user-1")
						.build()));

		var responses = courseService.getCoursesForUser("user-1");

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).id()).isEqualTo("course-1");
	}

	@Test
	void getCourseRejectsMissingOrForeignCourses() {
		when(courseRepository.findById("missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> courseService.getCourse("missing", "user-1"))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Course not found");

		when(courseRepository.findById("course-1")).thenReturn(Optional.of(Course.builder()
				.id("course-1")
				.title("React Basics")
				.creatorId("other-user")
				.build()));

		assertThatThrownBy(() -> courseService.getCourse("course-1", "user-1"))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Course does not belong");
	}
}
