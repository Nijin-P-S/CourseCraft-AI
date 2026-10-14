package com.coursecraft.ai.progress.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateLessonProgressRequest(
		@NotNull
		Boolean completed,

		@Min(0)
		@Max(100)
		Integer assessmentScore
) {
}
