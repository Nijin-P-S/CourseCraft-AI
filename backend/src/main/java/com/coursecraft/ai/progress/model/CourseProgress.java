package com.coursecraft.ai.progress.model;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
@Document(collection = "course_progress")
@CompoundIndex(
		name = "user_course_progress_unique",
		def = "{'userId': 1, 'courseId': 1}",
		unique = true
)
public class CourseProgress {

	@Id
	private String id;

	@NotBlank
	@Indexed
	private String userId;

	@NotBlank
	@Indexed
	private String courseId;

	private String lastAccessedLessonId;

	@Builder.Default
	private Set<String> completedLessonIds = new HashSet<>();

	@Builder.Default
	private Map<String, Integer> assessmentScores = new HashMap<>();

	@CreatedDate
	private Instant createdAt;

	@LastModifiedDate
	private Instant updatedAt;
}
