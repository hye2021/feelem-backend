package com.feelem.server.domain.filter;

import com.feelem.server.domain.sticker.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StickerRepository extends JpaRepository<Sticker, Long> {}

