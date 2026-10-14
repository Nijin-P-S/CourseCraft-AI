package com.coursecraft.ai.progress.service;

import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.repository.CourseRepository;
import com.coursecraft.ai.progress.dto.CourseProgressResponse;
import com.coursecraft.ai.progress.dto.UpdateLessonProgressRequest;
import com.coursecraft.ai.progress.model.CourseProgress;
import com.coursecraft.ai.progress.repository.CourseProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CourseProgressService {

	private final CourseRepository courseRepository;
	private final CourseProgressRepository courseProgressRepository;

	public CourseProgressResponse getProgress(String courseId, String userId) {
		verifyCourseAccess(courseId, userId);
		CourseProgress progress = getOrCreateProgress(courseId, userId);

		return CourseProgressResponse.from(progress);
	}

	public CourseProgressResponse updateLessonProgress(
			String courseId,
			String lessonId,
			String userId,
			UpdateLessonProgressRequest request
	) {
		Course course = verifyCourseAccess(courseId, userId);
		if (!lessonExists(course, lessonId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found");
		}

		CourseProgress progress = getOrCreateProgress(courseId, userId);
		progress.setLastAccessedLessonId(lessonId);

		if (request.completed()) {
			progress.getCompletedLessonIds().add(lessonId);
		} else {
			progress.getCompletedLessonIds().remove(lessonId);
		}

		if (request.assessmentScore() != null) {
			progress.getAssessmentScores().put(lessonId, request.assessmentScore());
		}

		return CourseProgressResponse.from(courseProgressRepository.save(progress));
	}

	private Course verifyCourseAccess(String courseId, String userId) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

		if (!course.getCreatorId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course does not belong to the authenticated user");
		}

		return course;
	}

	private CourseProgress getOrCreateProgress(String courseId, String userId) {
		return courseProgressRepository.findByUserIdAndCourseId(userId, courseId)
				.orElseGet(() -> courseProgressRepository.save(CourseProgress.builder()
						.userId(userId)
						.courseId(courseId)
						.build()));
	}

	private boolean lessonExists(Course course, String lessonId) {
		return course.getModules().stream()
				.flatMap(module -> module.getLessons().stream())
				.anyMatch(lesson -> lesson.getId().equals(lessonId));
	}
}
