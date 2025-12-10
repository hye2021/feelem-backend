import json
from typing import List, Dict, Any


# ---------------------------------------------------------
# 자카드 유사도 (집합 간의 교집합 비율)
# ---------------------------------------------------------
def _jaccard_similarity(list1: List[str], list2: List[str]) -> float:
    set1 = set(list1)
    set2 = set(list2)

    if not set1 and not set2:
        return 1.0  # 둘 다 비어있으면 완전히 같음

    intersection = set1.intersection(set2)
    union = set1.union(set2)

    if not union:
        return 0.0

    return len(intersection) / len(union)


# ---------------------------------------------------------
# 스티커 유사도 계산 (현재 자바 DTO - Boolean 호환)
# ---------------------------------------------------------
def calculate_sticker_similarity(
    profile1: Dict[str, Any], profile2: Dict[str, Any]
) -> float:
    """
    현재 Java DTO 구조에 맞춘 로직
    - count: int
    - placement_types: List[str]
    - has_face_sticker: bool
    """
    try:
        # 1. 배치 구역 유사도 (Placement) - 가중치 60%
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
        # 차이가 없으면 1.0, 5개 이상 차이나면 0.0
        count_score = max(0.0, 1.0 - (count_diff / 5.0))

        # 최종 가중 평균
        final_similarity = (
            (placement_score * 0.6) + (presence_score * 0.2) + (count_score * 0.2)
        )

        return final_similarity

    except Exception as e:
        print(f"⚠️ Error calculating sticker similarity: {e}")
        return 0.0


# ---------------------------------------------------------
# 후보군 재정렬 (JSON 파싱 로직 포함)
# ---------------------------------------------------------
def re_rank_candidates(input_metadatas: List[dict], candidates: dict) -> List[str]:
    """
    1차 조회된 200개(candidates)를 입력받아 Re-Rank를 수행하고 정렬된 ID 리스트를 반환
    **핵심 수정사항**: ChromaDB에서 꺼낸 메타데이터 문자열을 JSON으로 파싱하는 로직 추가
    """

    # 데이터 유효성 검사
    if not input_metadatas or not candidates:
        return []

    if not candidates.get("ids") or not candidates["ids"][0]:
        return []

    # 1. 입력 필터(사용자가 좋아한 필터)의 스티커 정보 파싱
    # ChromaDB 메타데이터는 단순 문자열(String)로 저장되어 있으므로 json.loads 필수
    try:
        raw_summary = input_metadatas[0].get("sticker_summary", "{}")

        if isinstance(raw_summary, str):
            avg_sticker_profile = json.loads(raw_summary)
        else:
            avg_sticker_profile = raw_summary  # 이미 dict인 경우

    except Exception as e:
        print(f"⚠️ Failed to parse input metadata: {e}")
        avg_sticker_profile = {}

    # 스티커 정보가 아예 없으면, 재정렬 없이 원본 순서(이미지 유사도 순) 반환
    if not avg_sticker_profile:
        return candidates["ids"][0]

    ranked_results = []

    candidate_ids = candidates["ids"][0]
    candidate_distances = candidates["distances"][0]
    candidate_metadatas = candidates["metadatas"][0]

    # 2. 후보군(200개) 순회하며 점수 다시 계산
    for i in range(len(candidate_ids)):

        # [A] 이미지 유사도 (Distance는 작을수록 좋으므로 1 - distance) -> 가중치 70%
        # ChromaDB distance가 보통 0~1 사이 (Cosine distance 기준)
        image_similarity_score = max(0.0, 1.0 - candidate_distances[i])

        # [B] 스티커 유사도 -> 가중치 30%
        try:
            raw_cand_summary = candidate_metadatas[i].get("sticker_summary", "{}")

            if isinstance(raw_cand_summary, str):
                candidate_sticker_profile = json.loads(raw_cand_summary)
            else:
                candidate_sticker_profile = raw_cand_summary

        except Exception:
            candidate_sticker_profile = {}

        sticker_similarity_score = calculate_sticker_similarity(
            avg_sticker_profile, candidate_sticker_profile
        )

        # [C] 최종 점수 합산
        final_score = (0.7 * image_similarity_score) + (0.3 * sticker_similarity_score)

        ranked_results.append((candidate_ids[i], final_score))

    # 3. 점수가 높은 순서대로 정렬 (내림차순)
    ranked_results.sort(key=lambda x: x[1], reverse=True)

    # 4. ID만 리스트로 추출하여 반환
    return [item[0] for item in ranked_results]
