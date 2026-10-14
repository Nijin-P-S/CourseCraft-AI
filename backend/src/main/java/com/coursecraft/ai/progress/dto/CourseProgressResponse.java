package com.coursecraft.ai.progress.dto;

import com.coursecraft.ai.progress.model.CourseProgress;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record CourseProgressResponse(
		String id,
		String userId,
		String courseId,
		String lastAccessedLessonId,
		Set<String> completedLessonIds,
		Map<String, Integer> assessmentScores,
		Instant createdAt,
		Instant updatedAt
) {
	public static CourseProgressResponse from(CourseProgress progress) {
		return new CourseProgressResponse(
				progress.getId(),
				progress.getUserId(),
				progress.getCourseId(),
				progress.getLastAccessedLessonId(),
				progress.getCompletedLessonIds(),
				progress.getAssessmentScores(),
				progress.getCreatedAt(),
				progress.getUpdatedAt()
		);
	}
}
