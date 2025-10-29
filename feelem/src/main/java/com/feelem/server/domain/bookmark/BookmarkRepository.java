package com.feelem.server.domain.bookmark;

import com.feelem.server.domain.filter.Filter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  Filter findByUserIdAndFilter(Long userId, Long filterId);

  boolean existsByUserAndFilter(com.feelem.server.domain.user.User user, Filter filter);

  java.util.Optional<Bookmark> findByUserAndFilter(com.feelem.server.domain.user.User user, Filter filter);
}
