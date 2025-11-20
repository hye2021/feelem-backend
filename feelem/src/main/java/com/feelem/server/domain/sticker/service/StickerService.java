package com.feelem.server.domain.sticker.service;

import com.feelem.server.domain.sticker.entity.StickerType;
import com.feelem.server.domain.sticker.repository.StickerRepository;
import com.feelem.server.domain.sticker.entity.Sticker;
import com.feelem.server.domain.user.entity.User;
import com.feelem.server.domain.user.service.UserService;
import com.feelem.server.domain.sticker.dto.StickerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StickerService {

  private final StickerRepository stickerRepository;
  private final UserService userService;

  public Sticker createSticker(StickerType type, String imageUrl) {
    User creator = userService.getCurrentUser();

    Sticker sticker = Sticker.builder()
        .creator(creator)
        .imageUrl(imageUrl)
        .stickerType(type)
        .build();

    System.out.println("✅ Sticker inserted ID: " + sticker.getId());

    return stickerRepository.save(sticker);
  }

  public List<StickerDto.Response> getStickers() {
    User user = userService.getCurrentUser();
    List<StickerDto.Response> response = stickerRepository.findByCreatorAndIsDeletedFalseOrderByCreatedAtDesc(user).stream()
        .map(StickerDto.Response::new)
        .toList();

    return response;
  }

  public void deleteSticker(Long id) {
    Sticker sticker = stickerRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 스티커를 찾을 수 없습니다. id=" + id));

    sticker.markAsDeleted();
    stickerRepository.save(sticker);
  }
}
