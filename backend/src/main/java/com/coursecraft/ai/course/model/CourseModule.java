package com.coursecraft.ai.course.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseModule {

	@NotBlank
	private String id;

	@NotBlank
	private String title;

	private String description;

	private int orderIndex;

	@Valid
	@Builder.Default
	private List<LessonSummary> lessons = new ArrayList<>();
}
