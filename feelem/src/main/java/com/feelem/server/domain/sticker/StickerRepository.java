package com.feelem.server.domain.sticker;

import com.feelem.server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StickerRepository extends JpaRepository<Sticker, Long> {

  List<Sticker> findByCreatorAndIsDeletedFalse(User creator);
}
