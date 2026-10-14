package com.coursecraft.ai.generation;

import org.springframework.stereotype.Component;

@Component
public class CoursePromptBuilder {

	public String buildCourseOutlinePrompt(String topic) {
		return """
				Generate a course outline for the topic: "%s".

				Return raw JSON only. Do not wrap the response in Markdown.
				The JSON must follow this exact shape:
				{
				  "title": "string",
				  "description": "string",
				  "tags": ["string"],
				  "modules": [
				    {
				      "title": "string",
				      "description": "string",
				      "lessons": ["string"]
				    }
				  ]
				}

				Rules:
				- Generate 3 to 6 modules.
				- Each module must contain 3 to 5 lesson titles.
				- Make the course progress from beginner concepts to practical application.
				- Keep tags short and lowercase.
				""".formatted(topic);
	}

	public String buildLessonContentPrompt(
			String courseTitle,
			String courseDescription,
			String moduleTitle,
			String moduleDescription,
			String lessonTitle
	) {
		return """
				Generate lesson content for this course lesson.

				Course title: "%s"
				Course description: "%s"
				Module title: "%s"
				Module description: "%s"
				Lesson title: "%s"

				Return raw JSON only. Do not wrap the response in Markdown.
				The JSON must follow this exact shape:
				{
				  "title": "string",
				  "objectives": ["string"],
				  "contentBlocks": [
				    {
				      "type": "heading",
				      "text": "string"
				    },
				    {
				      "type": "paragraph",
				      "text": "string"
				    },
				    {
				      "type": "video",
				      "query": "string"
				    },
				    {
				      "type": "mcq",
				      "question": "string",
				      "options": ["string"],
				      "answerIndex": 0,
				      "explanation": "string"
				    }
				  ]
				}

				Rules:
				- Generate 3 to 5 concise objectives.
				- Include at least one heading, two paragraphs, one video block, and two MCQ blocks.
				- Keep paragraph text practical and beginner-friendly.
				- Video blocks must contain a searchable YouTube-style query, not a URL.
				- MCQ answerIndex must be a zero-based index into options.
				""".formatted(courseTitle, courseDescription, moduleTitle, moduleDescription, lessonTitle);
	}
}
