package com.feelem.server.domain.bookmark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.feelem.server.domain.filter.Filter;
import com.feelem.server.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "bookmarks")
public class Bookmark {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 북마크한 유저
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private User user;

  // 북마크한 필터
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "filter_id", nullable = false)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private Filter filter;

  public Bookmark(User user, Filter filter) {
    this.user = user;
    this.filter = filter;
  }
}
