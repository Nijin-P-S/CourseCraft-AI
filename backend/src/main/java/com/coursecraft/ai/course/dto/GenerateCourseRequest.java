package com.coursecraft.ai.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateCourseRequest(
		@NotBlank
		@Size(max = 200)
		String topic
) {
}
