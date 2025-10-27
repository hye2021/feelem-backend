# Re-Rank 로직
# 비즈니스 로직에 맞춰 고도화 필요 ‼️

# app/services/ranking.py (전체 파일 내용)

import numpy as np
from typing import List


def _jaccard_similarity(list1: List[str], list2: List[str]) -> float:
    """
    [추가] 두 리스트 간의 자카드 유사도를 계산합니다.
    (교집합의 크기) / (합집합의 크기)
    """
    set1 = set(list1)
    set2 = set(list2)

    intersection = set1.intersection(set2)
    union = set1.union(set2)

    if not union:
        return 1.0  # 두 리스트가 모두 비어있으면 100% 일치

    return len(intersection) / len(union)


def calculate_sticker_similarity(profile1: dict, profile2: dict) -> float:
    """
    [수정] 두 필터의 스티커 메타데이터를 비교하여 유사도(0.0 ~ 1.0)를 반환합니다.
    - 배치 방식 유사도 (50%)
    - 스티커 종류 유사도 (30%)
    - 스티커 개수 유사도 (20%)
    """
    try:
        # 프로필 기본값 설정
        p1 = {
            "count": profile1.get("count", 0),
            "placement_types": profile1.get("placement_types", []),
            "sticker_types": profile1.get("sticker_types", []),
        }
        p2 = {
            "count": profile2.get("count", 0),
            "placement_types": profile2.get("placement_types", []),
            "sticker_types": profile2.get("sticker_types", []),
        }

        # 1. 배치 방식 유사도 (가중치 50%)
        placement_score = _jaccard_similarity(
            p1["placement_types"], p2["placement_types"]
        )

        # 2. 스티커 종류 유사도 (가중치 30%)
        type_score = _jaccard_similarity(p1["sticker_types"], p2["sticker_types"])

        # 3. 스티커 개수 유사도 (가중치 20%)
        # (차이가 0이면 1.0, 차이가 5개 이상이면 0.0)
        count_diff = abs(p1["count"] - p2["count"])
        count_score = max(0.0, 1.0 - (count_diff / 5.0))

        # 4. 최종 가중 평균
        final_similarity = (
            (placement_score * 0.5) + (type_score * 0.3) + (count_score * 0.2)
        )
        return final_similarity

    except Exception:
        return 0.0  # 예외 발생 시 유사도 0


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
