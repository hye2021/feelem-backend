package com.feelem.server.domain.sticker.repository;

import com.feelem.server.domain.sticker.entity.Sticker;
import com.feelem.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StickerRepository extends JpaRepository<Sticker, Long> {

  List<Sticker> findByCreatorAndIsDeletedFalse(User creator);

  List<Sticker> findByCreatorAndIsDeletedFalseOrderByCreatedAtDesc(User user);
}
