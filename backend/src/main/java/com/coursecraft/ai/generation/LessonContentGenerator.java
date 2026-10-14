package com.coursecraft.ai.generation;

import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.model.CourseModule;
import com.coursecraft.ai.course.model.LessonSummary;
import com.coursecraft.ai.lesson.model.LessonContent;

public interface LessonContentGenerator {

	LessonContent generate(Course course, CourseModule module, LessonSummary lesson);
}
