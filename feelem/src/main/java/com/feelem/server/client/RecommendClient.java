package com.feelem.server.client;

import com.feelem.server.client.RecommendDto.CacheUpdateResponseDto;
import com.feelem.server.client.RecommendDto.ExploreRequestDto;
import com.feelem.server.client.RecommendDto.IndexRequestDto;
import com.feelem.server.client.RecommendDto.IndexResponseDto;
import com.feelem.server.client.RecommendDto.RecommendationResponseDto;
import com.feelem.server.client.RecommendDto.SearchRequestDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "recommend-client", url = "${feelem.recommend-service.url}")
public interface RecommendClient {
  @PostMapping("/index")
  IndexResponseDto indexFilter(@RequestBody IndexRequestDto request);

  @PostMapping("/explore")
  RecommendationResponseDto getExplore(@RequestBody ExploreRequestDto request);

  @PostMapping("/search")
  RecommendationResponseDto search(@RequestBody SearchRequestDto request);

  @PostMapping("/cache/refresh/{filter_id}")
  CacheUpdateResponseDto refreshCache(@PathVariable("filter_id") Long filterId);
}
