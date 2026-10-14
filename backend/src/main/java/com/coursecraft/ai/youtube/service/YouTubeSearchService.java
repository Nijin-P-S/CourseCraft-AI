package com.coursecraft.ai.youtube.service;

import com.coursecraft.ai.youtube.dto.YouTubeVideoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class YouTubeSearchService {

	private final RestClient restClient;
	private final String apiKey;

	public YouTubeSearchService(@Value("${app.youtube.api-key}") String apiKey) {
		this.restClient = RestClient.create("https://www.googleapis.com");
		this.apiKey = apiKey;
	}

	public YouTubeVideoResponse searchFirstVideo(String query) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "YouTube API key is not configured");
		}

		JsonNode response = restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/youtube/v3/search")
						.queryParam("part", "snippet")
						.queryParam("type", "video")
						.queryParam("maxResults", 1)
						.queryParam("q", query)
						.queryParam("key", apiKey)
						.build())
				.retrieve()
				.body(JsonNode.class);

		JsonNode firstItem = response == null ? null : response.path("items").path(0);
		if (firstItem == null || firstItem.isMissingNode()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No YouTube video found for query");
		}

		String videoId = firstItem.path("id").path("videoId").asText();
		if (videoId.isBlank()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No YouTube video found for query");
		}

		JsonNode snippet = firstItem.path("snippet");
		return new YouTubeVideoResponse(
				videoId,
				"https://www.youtube.com/embed/" + videoId,
				"https://www.youtube.com/watch?v=" + videoId,
				snippet.path("title").asText(),
				snippet.path("channelTitle").asText(),
				readThumbnailUrl(snippet.path("thumbnails"))
		);
	}

	private String readThumbnailUrl(JsonNode thumbnailsNode) {
		JsonNode high = thumbnailsNode.path("high").path("url");
		if (!high.asText().isBlank()) {
			return high.asText();
		}

		JsonNode medium = thumbnailsNode.path("medium").path("url");
		if (!medium.asText().isBlank()) {
			return medium.asText();
		}

		return thumbnailsNode.path("default").path("url").asText();
	}
}
