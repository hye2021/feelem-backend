# Re-Rank 로직
# 비즈니스 로직에 맞춰 고도화 필요 ‼️

# app/services/ranking.py (전체 파일 내용)

import numpy as np
from typing import List


def _jaccard_similarity(list1: List[str], list2: List[str]) -> float:
    # (기존과 동일)
    set1 = set(list1)
    set2 = set(list2)
    intersection = set1.intersection(set2)
    union = set1.union(set2)
    if not union:
        return 1.0
    return len(intersection) / len(union)


def calculate_sticker_similarity(profile1: dict, profile2: dict) -> float:
    """
    [수정] 얼굴 인식 스티커 기반 유사도 계산
    1. 배치 유사도 (60%)
    2. 사용 여부 일치 (20%)
    3. 개수 유사도 (20%)
    """
    try:
        # 1. 배치 유사도 (Placement) - 가중치 60%
        # (얼굴의 어느 부위에 붙였는지가 스타일을 결정함)
        placement_score = _jaccard_similarity(
            profile1.get("placement_types", []), profile2.get("placement_types", [])
        )

        # 2. 사용 여부 일치 (Boolean) - 가중치 20%
        # 둘 다 True거나 둘 다 False면 1.0, 다르면 0.0
        has_face1 = profile1.get("has_face_sticker", False)
        has_face2 = profile2.get("has_face_sticker", False)

        presence_score = 1.0 if has_face1 == has_face2 else 0.0

        # 3. 개수 유사도 (Count) - 가중치 20%
        c1 = profile1.get("count", 0)
        c2 = profile2.get("count", 0)
        count_diff = abs(c1 - c2)
        count_score = max(0.0, 1.0 - (count_diff / 5.0))  # 5개 이상 차이나면 0점

        # 최종 가중 평균
        final_similarity = (
            (placement_score * 0.6) + (presence_score * 0.2) + (count_score * 0.2)
        )

        return final_similarity

    except Exception:
        return 0.0


def re_rank_candidates(input_metadatas: List[dict], candidates: dict) -> List[str]:
    """
    [수정] 1차 조회된 200개(candidates)를 입력받아 Re-Rank를 수행하고 정렬된 ID 리스트를 반환합니다.
    """

    # 1. 입력 필터 2개의 '평균 스티커 프로필' 계산
    # (여러 필터의 프로필을 평균내는 것은 복잡하므로, 여기서는 첫 번째 필터의 프로필을 기준으로 사용합니다.)
    if not input_metadatas:
        # 입력 필터 정보가 없으면 Re-Rank 불가, 원본(이미지 유사도) 순서 반환
        return candidates["ids"][0]

    avg_sticker_profile = input_metadatas[0].get("sticker_summary", {})
    if not avg_sticker_profile:
        # 입력 필터에 스티커 정보가 없으면 Re-Rank 중단
        return candidates["ids"][0]

    ranked_results = []

    if not candidates or not candidates.get("ids") or not candidates["ids"][0]:
        return []  # 빈 리스트 반환 (예외 방지)

    candidate_ids = candidates["ids"][0]
    candidate_distances = candidates["distances"][0]
    candidate_metadatas = candidates["metadatas"][0]

    for i in range(len(candidate_ids)):
        # 1. 이미지 유사도 (가중치 70%)
        image_similarity_score = 1.0 - candidate_distances[i]

        candidate_sticker_profile = candidate_metadatas[i].get("sticker_summary", {})

        # 2. 스티커 유사도 (가중치 30%)
        sticker_similarity_score = calculate_sticker_similarity(
            avg_sticker_profile, candidate_sticker_profile
        )

        # 최종 점수 (이미지 70%, 스티커 30%)
        # (이 가중치는 서비스 방향에 맞게 조절하세요)
        final_score = (0.7 * image_similarity_score) + (0.3 * sticker_similarity_score)
        ranked_results.append((candidate_ids[i], final_score))

    # 최종 점수 기준 내림차순 정렬
    ranked_results.sort(key=lambda x: x[1], reverse=True)

    # ID 리스트만 반환
    return [item[0] for item in ranked_results]
