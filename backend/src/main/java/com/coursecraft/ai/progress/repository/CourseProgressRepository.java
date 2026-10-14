package com.coursecraft.ai.progress.repository;

import com.coursecraft.ai.progress.model.CourseProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CourseProgressRepository extends MongoRepository<CourseProgress, String> {

	Optional<CourseProgress> findByUserIdAndCourseId(String userId, String courseId);

	List<CourseProgress> findByUserId(String userId);
}
