package com.coursecraft.ai.course.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonSummary {

	@NotBlank
	private String id;

	@NotBlank
	private String title;

	private int orderIndex;

	private boolean contentGenerated;
}
