package com.feelem.server.domain.bookmark;

import com.feelem.server.domain.filter.FilterService;
import com.feelem.server.domain.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // ✅ 의존성 자동 주입
public class BookmarkService {

  private final FilterService filterService;
  private final UserService userService;
  private final BookmarkRepository bookmarkRepository;

  // 북마크 추가
  @Transactional
  public void addBookmark(long filterId) {
    var user = userService.getCurrentUser();
    var filter = filterService.findById(filterId);

    // 중복 방지
    if (bookmarkRepository.existsByUserAndFilter(user, filter)) {
      throw new IllegalStateException("Filter already bookmarked by user");
    }

    bookmarkRepository.save(new Bookmark(user, filter));
  }

  // 북마크 취소
  @Transactional
  public void removeBookmark(long filterId) {
    var user = userService.getCurrentUser();
    var filter = filterService.findById(filterId);

    var bookmark = bookmarkRepository.findByUserAndFilter(user, filter)
        .orElseThrow(() -> new EntityNotFoundException("Bookmark not found"));

    bookmarkRepository.delete(bookmark);
  }
}
