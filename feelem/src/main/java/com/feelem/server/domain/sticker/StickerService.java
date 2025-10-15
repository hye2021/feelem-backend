package com.feelem.server.domain.sticker;

import com.feelem.server.domain.user.User;
import com.feelem.server.domain.user.UserService;
import com.feelem.server.global.dto.StickerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StickerService {

  private final StickerRepository stickerRepository;
  private final UserService userService;

  @Transactional
  public Sticker createSticker(StickerDto.CreateRequest request) {
    User creator = userService.getCurrentUser();

    Sticker sticker = Sticker.builder()
        .creator(creator)
        .imageUrl(request.getImageUrl())
        .stickerType(request.getType())
        .build();

    return stickerRepository.save(sticker);
  }

  public List<StickerDto.Response> getStickers() {
    User user = userService.getCurrentUser();
    List<StickerDto.Response> response = stickerRepository.findByCreatorAndIsDeletedFalse(user).stream()
        .map(StickerDto.Response::new)
        .toList();

    return response;
  }

  @Transactional
  public void deleteSticker(Long id) {
    Sticker sticker = stickerRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 스티커를 찾을 수 없습니다. id=" + id));

    sticker.markAsDeleted();
    stickerRepository.save(sticker);
  }
}
