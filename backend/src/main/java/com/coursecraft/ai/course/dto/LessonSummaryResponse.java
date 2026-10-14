package com.coursecraft.ai.course.dto;

import com.coursecraft.ai.course.model.LessonSummary;

public record LessonSummaryResponse(
		String id,
		String title,
		int orderIndex,
		boolean contentGenerated
) {
	public static LessonSummaryResponse from(LessonSummary lesson) {
		return new LessonSummaryResponse(
				lesson.getId(),
				lesson.getTitle(),
				lesson.getOrderIndex(),
				lesson.isContentGenerated()
		);
	}
}
