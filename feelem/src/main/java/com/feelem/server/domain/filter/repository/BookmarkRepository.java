package com.feelem.server.domain.filter.repository;

import com.feelem.server.domain.filter.entity.Bookmark;
import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  boolean existsByUserAndFilter(User user, Filter filter);

  void deleteByUserAndFilter(User user, Filter filter);

  Optional<Bookmark> findByUserAndFilter(User user, Filter filter);

  /**
   * 북마크된 필터 목록을 바로 Filter 기준으로 페이징 조회한다.
   * createdAt 기준 내림차순 정렬
   */
  @Query("""
        SELECT b.filter
        FROM Bookmark b
        WHERE b.user.id = :userId
        ORDER BY b.createdAt DESC
    """)
  Page<Filter> findBookmarkedFilters(Long userId, Pageable pageable);

  @Query("""
      SELECT b.filter.id
      FROM Bookmark b
      WHERE b.user.id = :userId
      ORDER BY b.createdAt DESC
  """)
  List<Long> findRecentBookmarkedFilterIds(@Param("userId") Long userId, Pageable pageable);
}
