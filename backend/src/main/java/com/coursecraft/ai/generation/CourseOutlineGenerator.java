package com.coursecraft.ai.generation;

import com.coursecraft.ai.course.model.Course;

public interface CourseOutlineGenerator {

	Course generate(String topic, String creatorId);
}
