package com.feelem.server.domain.bookmark.repository;

import com.feelem.server.domain.bookmark.entity.Bookmark;
import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  Filter findByUserIdAndFilter(Long userId, Long filterId);

  boolean existsByUserAndFilter(User user, Filter filter);

  java.util.Optional<Bookmark> findByUserAndFilter(User user, Filter filter);
}
