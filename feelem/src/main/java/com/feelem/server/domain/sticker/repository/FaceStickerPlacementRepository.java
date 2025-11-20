package com.feelem.server.domain.sticker.repository;

import com.feelem.server.domain.sticker.entity.FaceStickerPlacement;
import com.feelem.server.domain.sticker.entity.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaceStickerPlacementRepository extends JpaRepository<FaceStickerPlacement, Long> {

}
