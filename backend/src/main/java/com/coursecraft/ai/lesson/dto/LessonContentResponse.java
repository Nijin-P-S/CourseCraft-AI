package com.coursecraft.ai.lesson.dto;

import com.coursecraft.ai.lesson.model.LessonContent;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record LessonContentResponse(
		String id,
		String courseId,
		String moduleId,
		String lessonId,
		String title,
		List<String> objectives,
		List<Map<String, Object>> contentBlocks,
		Map<String, Object> metadata,
		Instant createdAt,
		Instant updatedAt
) {
	public static LessonContentResponse from(LessonContent lessonContent) {
		return new LessonContentResponse(
				lessonContent.getId(),
				lessonContent.getCourseId(),
				lessonContent.getModuleId(),
				lessonContent.getLessonId(),
				lessonContent.getTitle(),
				lessonContent.getObjectives(),
				lessonContent.getContentBlocks(),
				lessonContent.getMetadata(),
				lessonContent.getCreatedAt(),
				lessonContent.getUpdatedAt()
		);
	}
}
