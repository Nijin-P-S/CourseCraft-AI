package com.coursecraft.ai.course.repository;

import com.coursecraft.ai.course.model.Course;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CourseRepository extends MongoRepository<Course, String> {

	List<Course> findByCreatorIdOrderByCreatedAtDesc(String creatorId);
}
