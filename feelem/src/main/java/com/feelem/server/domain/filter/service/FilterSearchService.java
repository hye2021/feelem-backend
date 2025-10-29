package com.feelem.server.domain.filter.service;

import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.filter.repository.FilterTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FilterSearchService {

  private final FilterTagRepository filterTagRepository;
  private static final int PAGE_SIZE = 20;

  /**
   * 태그 검색 (20개 단위 페이지네이션)
   */
  public Page<Filter> searchByTag(String keyword, int page) {
    PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
    return filterTagRepository.findFiltersByTagName(keyword, pageRequest);
  }
}