package com.coursecraft.ai.youtube.service;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.client.MockRestServiceServer;

class YouTubeSearchServiceTests {

	@Test
	void searchFirstVideoReturnsFirstVideo() {
		RestClient.Builder restClientBuilder = RestClient.builder().baseUrl("https://www.googleapis.com");
		MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
		YouTubeSearchService service = new YouTubeSearchService(
				"youtube-key",
				restClientBuilder.build(),
				new ObjectMapper()
		);

		server.expect(requestTo(allOf(
						containsString("/youtube/v3/search"),
						containsString("part=snippet"),
						containsString("type=video"),
						containsString("maxResults=1"),
						containsString("q=react%20hooks"),
						containsString("key=youtube-key")
				)))
				.andRespond(withSuccess("""
						{
						  "items": [
						    {
						      "id": { "videoId": "abc123" },
						      "snippet": {
						        "title": "React Hooks",
						        "channelTitle": "CourseCraft",
						        "thumbnails": {
						          "high": { "url": "https://img.youtube.com/high.jpg" }
						        }
						      }
						    }
						  ]
						}
						""", MediaType.APPLICATION_JSON));

		var response = service.searchFirstVideo("react hooks");

		assertThat(response.videoId()).isEqualTo("abc123");
		assertThat(response.embedUrl()).isEqualTo("https://www.youtube.com/embed/abc123");
		assertThat(response.watchUrl()).isEqualTo("https://www.youtube.com/watch?v=abc123");
		assertThat(response.title()).isEqualTo("React Hooks");
		assertThat(response.channelTitle()).isEqualTo("CourseCraft");
		assertThat(response.thumbnailUrl()).isEqualTo("https://img.youtube.com/high.jpg");
		server.verify();
	}

	@Test
	void searchFirstVideoMapsYouTubeErrors() {
		RestClient.Builder restClientBuilder = RestClient.builder().baseUrl("https://www.googleapis.com");
		MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
		YouTubeSearchService service = new YouTubeSearchService(
				"youtube-key",
				restClientBuilder.build(),
				new ObjectMapper()
		);

		server.expect(requestTo(containsString("/youtube/v3/search")))
				.andRespond(withStatus(HttpStatus.FORBIDDEN)
						.contentType(MediaType.APPLICATION_JSON)
						.body("""
								{
								  "error": {
								    "message": "API key not valid"
								  }
								}
								"""));

		assertThatThrownBy(() -> service.searchFirstVideo("react hooks"))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("502 BAD_GATEWAY")
				.hasMessageContaining("API key not valid");
		server.verify();
	}
}
