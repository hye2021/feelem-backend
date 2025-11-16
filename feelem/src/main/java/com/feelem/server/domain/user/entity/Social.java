package com.feelem.server.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "socials")
public class Social {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "instagram_id")
  private String instagramId;

  @Column(name = "x_id")
  private String xId;


  public Social(User user, String instagramId, String xId) {
    this.user = user;
    this.instagramId = instagramId;
    this.xId = xId;
  }

  public void update(String instagramId, String xId) {
    this.instagramId = instagramId;
    this.xId = xId;
  }
}
