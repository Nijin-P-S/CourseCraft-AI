package com.coursecraft.ai.youtube.dto;

public record YouTubeVideoResponse(
		String videoId,
		String embedUrl,
		String watchUrl,
		String title,
		String channelTitle,
		String thumbnailUrl
) {
}
