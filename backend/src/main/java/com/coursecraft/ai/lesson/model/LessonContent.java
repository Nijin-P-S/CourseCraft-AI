package com.coursecraft.ai.lesson.model;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "lesson_contents")
@CompoundIndex(
		name = "course_module_lesson_unique",
		def = "{'courseId': 1, 'moduleId': 1, 'lessonId': 1}",
		unique = true
)
public class LessonContent {

	@Id
	private String id;

	@NotBlank
	@Indexed
	private String courseId;

	@NotBlank
	private String moduleId;

	@NotBlank
	private String lessonId;

	@NotBlank
	private String title;

	@Builder.Default
	private List<String> objectives = new ArrayList<>();

	@Builder.Default
	private List<Map<String, Object>> contentBlocks = new ArrayList<>();

	@Builder.Default
	private Map<String, Object> metadata = new HashMap<>();

	@CreatedDate
	private Instant createdAt;

	@LastModifiedDate
	private Instant updatedAt;
}
