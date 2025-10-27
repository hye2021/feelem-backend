# 서버가 시작될 때, MySQL에 접속하여 추천 계산에 자주 필요한 정보들을
# 미리 읽어와 메모리(RAM)에 저장

# app/cache.py
import os
import mysql.connector
import math
from typing import Dict, Any

# 각 필터(ID) 별로 스티커 속성을 저장하는 딕셔너리
filter_sticker_meta: Dict[int, Dict[str, Any]] = {}
# 각 필터(ID) 별로 인기도 점수를 저장하는 딕셔너리
filter_popularity_meta: Dict[int, float] = {}


# 서버가 시작될 때 1번 호출
def load_caches_from_db():
    print("Loading metadata caches from RDS (MySQL)...")
    conn = None
    try:
        conn = mysql.connector.connect(
            host=os.environ.get("RDS_HOST"),
            database=os.environ.get("RDS_DB_NAME"),
            user=os.environ.get("RDS_USERNAME"),
            password=os.environ.get("RDS_PASSWORD"),
            port=int(os.environ.get("RDS_PORT", 3306)),
        )
        cursor = conn.cursor(dictionary=True)  # 결과를 dict로 받기

        # 1. 스티커 메타데이터 로드
        cursor.execute(
            """
            SELECT 
                fs.filter_id,
                MAX(CASE WHEN s.sticker_type = 'AI' THEN 1 ELSE 0 END) as has_ai_sticker,
                MAX(CASE WHEN s.sticker_type = 'BRUSH' THEN 1 ELSE 0 END) as has_brush_sticker,
                MAX(CASE WHEN s.sticker_type = 'IMAGE' THEN 1 ELSE 0 END) as has_image_sticker,
                MAX(CASE WHEN fs.placement_type = 'FACE_TRACKING' THEN 1 ELSE 0 END) as has_face_placement
            FROM filter_stickers fs
            JOIN stickers s ON fs.sticker_id = s.id
            GROUP BY fs.filter_id
        """
        )

        default_sticker_meta = {
            "has_ai_sticker": False,
            "has_brush_sticker": False,
            "has_image_sticker": False,
            "has_face_placement": False,
        }

        for row in cursor.fetchall():
            filter_sticker_meta[int(row["filter_id"])] = {
                "has_ai_sticker": bool(row["has_ai_sticker"]),
                "has_brush_sticker": bool(row["has_brush_sticker"]),
                "has_image_sticker": bool(row["has_image_sticker"]),
                "has_face_placement": bool(row["has_face_placement"]),
            }

        # 2. 인기도 메타데이터 로드
        cursor.execute(
            "SELECT id, save_count, use_count FROM filters WHERE is_deleted = 0"
        )
        for row in cursor.fetchall():
            filter_id = int(row["id"])  # 변수명 통일 (id -> filter_id)
            popularity = math.log10(row["save_count"] + row["use_count"] + 1)
            filter_popularity_meta[filter_id] = popularity

            # 스티커 없는 필터도 기본값으로 채워넣기
            if filter_id not in filter_sticker_meta:
                filter_sticker_meta[filter_id] = default_sticker_meta.copy()

        cursor.close()

        print(
            f"✅ Cache loading complete. Filters loaded: {len(filter_popularity_meta)}"
        )

    except Exception as e:
        print(f"❌ Error loading cache from MySQL: {e}")
    finally:
        if conn and conn.is_connected():
            conn.close()


# 캐시 조회용 헬퍼 함수
def get_sticker_meta(filter_id: int) -> Dict[str, Any]:
    # 스티커 없는 필터도 기본값이 있으므로 .get()으로 안전하게 조회
    return filter_sticker_meta.get(
        filter_id,
        {
            "has_ai_sticker": False,
            "has_brush_sticker": False,
            "has_image_sticker": False,
            "has_face_placement": False,
        },
    )


def get_popularity_score(filter_id: int) -> float:
    return filter_popularity_meta.get(filter_id, 0.0)


def update_single_filter_cache(filter_id: int):
    """
    (Spring 서버가 호출) 특정 필터 ID의 캐시만 새로고침합니다.
    (인기도 + 스티커 속성)
    """
    print(f"Refreshing cache for filter_id: {filter_id}...")
    conn = None
    try:
        conn = mysql.connector.connect(
            host=os.environ.get("RDS_HOST"),
            database=os.environ.get("RDS_DB_NAME"),
            user=os.environ.get("RDS_USERNAME"),
            password=os.environ.get("RDS_PASSWORD"),
            port=int(os.environ.get("RDS_PORT", 3306)),
        )
        cursor = conn.cursor(dictionary=True)

        # 1. 인기도 점수 업데이트 (삭제 여부 확인 포함)
        cursor.execute(
            # (수정) AND is_deleted = 0 추가
            "SELECT save_count, use_count FROM filters WHERE id = %s AND is_deleted = 0",
            (filter_id,),
        )
        popularity_row = cursor.fetchone()

        if popularity_row:
            # --- 필터가 존재하고 삭제되지 않은 경우 ---
            # 1-1. 인기도 캐시 업데이트
            popularity = math.log10(
                popularity_row["save_count"] + popularity_row["use_count"] + 1
            )
            filter_popularity_meta[filter_id] = popularity

            # 1-2. 스티커 속성 업데이트
            cursor.execute(
                """
                SELECT 
                    MAX(CASE WHEN s.sticker_type = 'AI' THEN 1 ELSE 0 END) as has_ai_sticker,
                    MAX(CASE WHEN s.sticker_type = 'BRUSH' THEN 1 ELSE 0 END) as has_brush_sticker,
                    MAX(CASE WHEN s.sticker_type = 'IMAGE' THEN 1 ELSE 0 END) as has_image_sticker,
                    MAX(CASE WHEN fs.placement_type = 'FACE_TRACKING' THEN 1 ELSE 0 END) as has_face_placement
                FROM filter_stickers fs
                JOIN stickers s ON fs.sticker_id = s.id
                WHERE fs.filter_id = %s
                GROUP BY fs.filter_id
            """,
                (filter_id,),
            )
            sticker_row = cursor.fetchone()

            if sticker_row:
                filter_sticker_meta[filter_id] = {
                    "has_ai_sticker": bool(sticker_row["has_ai_sticker"]),
                    "has_brush_sticker": bool(sticker_row["has_brush_sticker"]),
                    "has_image_sticker": bool(sticker_row["has_image_sticker"]),
                    "has_face_placement": bool(sticker_row["has_face_placement"]),
                }
            else:
                # 스티커가 없는 경우 (기본값)
                filter_sticker_meta[filter_id] = {
                    "has_ai_sticker": False,
                    "has_brush_sticker": False,
                    "has_image_sticker": False,
                    "has_face_placement": False,
                }
            print(f"✅ Cache updated for filter_id: {filter_id}")

        else:
            # --- 필터가 삭제되었거나 존재하지 않는 경우 ---
            # 캐시에서 해당 필터 정보를 제거합니다.
            filter_popularity_meta.pop(filter_id, None)
            filter_sticker_meta.pop(filter_id, None)
            print(f"✅ Filter {filter_id} is deleted or not found. Removed from cache.")

        cursor.close()
        return True

    except Exception as e:
        print(f"❌ Error updating single cache for {filter_id}: {e}")
        return False
    finally:
        if conn and conn.is_connected():
            conn.close()
