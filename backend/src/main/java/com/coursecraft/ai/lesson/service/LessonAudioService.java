package com.coursecraft.ai.lesson.service;

import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.model.CourseModule;
import com.coursecraft.ai.course.model.LessonSummary;
import com.coursecraft.ai.course.repository.CourseRepository;
import com.coursecraft.ai.lesson.model.LessonContent;
import com.coursecraft.ai.lesson.repository.LessonContentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LessonAudioService {

	private final CourseRepository courseRepository;
	private final LessonContentRepository lessonContentRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final RestClient restClient = RestClient.create("https://generativelanguage.googleapis.com");

	@Value("${app.ai.gemini.api-key}")
	private String geminiApiKey;

	@Value("${app.ai.gemini.tts-model}")
	private String ttsModel;

	@Value("${app.ai.gemini.tts-voice}")
	private String ttsVoice;

	public byte[] generateHinglishAudio(String courseId, String moduleId, String lessonId, String userId) {
		if (geminiApiKey == null || geminiApiKey.isBlank()) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Hinglish audio is not configured yet");
		}

		Course course = getOwnedCourse(courseId, userId);
		CourseModule module = findModule(course, moduleId);
		LessonSummary lesson = findLesson(module, lessonId);
		LessonContent content = lessonContentRepository
				.findByCourseIdAndModuleIdAndLessonId(courseId, moduleId, lessonId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson content not found"));

		String responseBody;
		try {
			responseBody = restClient.post()
					.uri("/v1beta/models/{model}:generateContent?key={apiKey}", ttsModel, geminiApiKey)
					.body(buildTtsRequest(course, module, lesson, content))
					.retrieve()
					.body(String.class);
		} catch (RestClientResponseException exception) {
			throw new ResponseStatusException(
					HttpStatus.BAD_GATEWAY,
					"Hinglish audio is currently unavailable. Please try again later.",
					exception
			);
		}

		return readAudio(responseBody);
	}

	private Map<String, Object> buildTtsRequest(
			Course course,
			CourseModule module,
			LessonSummary lesson,
			LessonContent content
	) {
		Map<String, Object> part = Map.of("text", buildNarrationPrompt(course, module, lesson, content));
		Map<String, Object> generationConfig = Map.of(
				"responseModalities", List.of("AUDIO"),
				"speechConfig", Map.of(
						"voiceConfig", Map.of(
								"prebuiltVoiceConfig", Map.of("voiceName", ttsVoice)
						)
				)
		);

		return Map.of(
				"contents", List.of(Map.of("parts", List.of(part))),
				"generationConfig", generationConfig
		);
	}

	private String buildNarrationPrompt(
			Course course,
			CourseModule module,
			LessonSummary lesson,
			LessonContent content
	) {
		return """
				Explain this lesson in friendly Hinglish for an Indian student.
				Use simple conversational language, keep technical terms in English when helpful,
				and do not mention JSON, blocks, or formatting. Keep it under 4 minutes.

				Course: %s
				Module: %s
				Lesson: %s

				Lesson content:
				%s
				""".formatted(course.getTitle(), module.getTitle(), lesson.getTitle(), lessonText(content));
	}

	private String lessonText(LessonContent content) {
		StringBuilder builder = new StringBuilder();
		builder.append(content.getTitle()).append("\n\n");

		if (!content.getObjectives().isEmpty()) {
			builder.append("Objectives:\n");
			content.getObjectives().forEach(objective -> builder.append("- ").append(objective).append('\n'));
			builder.append('\n');
		}

		content.getContentBlocks().forEach(block -> appendBlockText(builder, block));
		String text = builder.toString().trim();
		return text.length() > 6000 ? text.substring(0, 6000) : text;
	}

	private void appendBlockText(StringBuilder builder, Map<String, Object> block) {
		String type = String.valueOf(block.getOrDefault("type", ""));
		switch (type) {
			case "heading", "paragraph", "note" -> appendValue(builder, block.get("text"));
			case "code" -> appendValue(builder, block.getOrDefault("code", block.get("text")));
			case "video" -> appendValue(builder, block.getOrDefault("query", block.get("title")));
			case "mcq" -> {
				appendValue(builder, block.get("question"));
				appendValue(builder, block.get("explanation"));
			}
			default -> {
			}
		}
	}

	private void appendValue(StringBuilder builder, Object value) {
		if (value instanceof String text && !text.isBlank()) {
			builder.append(text).append("\n\n");
		}
	}

	private byte[] readAudio(String responseBody) {
		try {
			JsonNode part = objectMapper.readTree(responseBody)
					.path("candidates")
					.path(0)
					.path("content")
					.path("parts")
					.path(0);
			JsonNode inlineData = part.has("inlineData") ? part.path("inlineData") : part.path("inline_data");
			String base64Audio = inlineData.path("data").asText();
			String mimeType = inlineData.path("mimeType").asText();

			if (base64Audio.isBlank()) {
				throw new IllegalStateException("Gemini TTS response did not contain audio data");
			}

			byte[] audioBytes = Base64.getDecoder().decode(base64Audio);
			if (mimeType.toLowerCase().contains("wav")) {
				return audioBytes;
			}

			return toWav(audioBytes, sampleRateFrom(mimeType));
		} catch (Exception exception) {
			throw new ResponseStatusException(
					HttpStatus.BAD_GATEWAY,
					"Hinglish audio is currently unavailable. Please try again later.",
					exception
			);
		}
	}

	private int sampleRateFrom(String mimeType) {
		if (mimeType == null) {
			return 24000;
		}

		String marker = "rate=";
		int markerIndex = mimeType.indexOf(marker);
		if (markerIndex < 0) {
			return 24000;
		}

		String rate = mimeType.substring(markerIndex + marker.length()).replaceAll("[^0-9].*$", "");
		return rate.isBlank() ? 24000 : Integer.parseInt(rate);
	}

	private byte[] toWav(byte[] pcmData, int sampleRate) {
		int byteRate = sampleRate * 2;
		ByteArrayOutputStream output = new ByteArrayOutputStream(44 + pcmData.length);
		writeAscii(output, "RIFF");
		writeInt(output, 36 + pcmData.length);
		writeAscii(output, "WAVEfmt ");
		writeInt(output, 16);
		writeShort(output, 1);
		writeShort(output, 1);
		writeInt(output, sampleRate);
		writeInt(output, byteRate);
		writeShort(output, 2);
		writeShort(output, 16);
		writeAscii(output, "data");
		writeInt(output, pcmData.length);
		output.writeBytes(pcmData);
		return output.toByteArray();
	}

	private void writeAscii(ByteArrayOutputStream output, String value) {
		output.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
	}

	private void writeInt(ByteArrayOutputStream output, int value) {
		output.writeBytes(ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array());
	}

	private void writeShort(ByteArrayOutputStream output, int value) {
		output.writeBytes(ByteBuffer.allocate(Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).putShort((short) value).array());
	}

	private Course getOwnedCourse(String courseId, String userId) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

		if (!course.getCreatorId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course does not belong to the authenticated user");
		}

		return course;
	}

	private CourseModule findModule(Course course, String moduleId) {
		return course.getModules().stream()
				.filter(module -> module.getId().equals(moduleId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
	}

	private LessonSummary findLesson(CourseModule module, String lessonId) {
		return module.getLessons().stream()
				.filter(lesson -> lesson.getId().equals(lessonId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
	}
}
