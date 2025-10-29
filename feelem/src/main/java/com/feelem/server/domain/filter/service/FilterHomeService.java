package com.feelem.server.domain.filter.service;

import com.feelem.server.domain.filter.dto.FilterSearchResponse;
import com.feelem.server.domain.filter.repository.FilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilterHomeService {

  private final FilterRepository filterRepository;

  /**
   * 홈화면용 필터 조회 (최신순 페이징)
   */
  public Page<FilterSearchResponse> getRecentFilters(Pageable pageable) {
    return filterRepository.findAllByIsDeletedFalseOrderByCreatedAtDesc(pageable)
        .map(FilterSearchResponse::from);
  }
}
