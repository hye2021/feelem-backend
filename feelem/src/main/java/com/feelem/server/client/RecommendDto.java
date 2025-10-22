package com.feelem.server.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

public class RecommendDto {
  // Index
  @Getter
  @AllArgsConstructor
  public static class IndexRequestDto {
    private Long filterId;
    private String editedImageUrl;
  }
  @Getter
  @NoArgsConstructor
  public static class IndexResponseDto {
    private boolean success;
    private Long filterId;
  }

  // ForYou
  @Getter
  @AllArgsConstructor
  public static class ExploreRequestDto {
    private String userId;
    private List<Long> recentFilterIds;
  }

  // Search
  @Getter
  @AllArgsConstructor
  public static class SearchRequestDto {
    private String userId;
    private String queryText;
    private List<Long> recentFilterIds;
  }

  // Common Response
  @Getter
  @NoArgsConstructor
  public static class RecommendationResponseDto {
    private List<Long> filterIds;
  }

  @Getter
  @NoArgsConstructor
  public static class CacheUpdateResponseDto {
    private boolean success;
    private Long filterId;
  }
}
