package com.coursecraft.ai.lesson.repository;

import com.coursecraft.ai.lesson.model.LessonContent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LessonContentRepository extends MongoRepository<LessonContent, String> {

	Optional<LessonContent> findByCourseIdAndModuleIdAndLessonId(
			String courseId,
			String moduleId,
			String lessonId
	);

	List<LessonContent> findByCourseId(String courseId);
}
