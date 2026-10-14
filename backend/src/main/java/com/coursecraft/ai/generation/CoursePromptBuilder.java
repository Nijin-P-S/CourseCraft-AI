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
}
