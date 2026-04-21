/* 1. 기존 키워드 데이터 초기화 */
TRUNCATE TABLE keywords;

/* 2. 업종별 키워드 데이터 삽입 */
-- 카페 / 베이커리 (CAFE)
INSERT INTO keywords (industry_code, category, NAME) VALUES 
('CAFE', '기본', '감성'), ('CAFE', '기본', '가성비'), ('CAFE', '기본', '신메뉴'), 
('CAFE', '기본', '위치'), ('CAFE', '기본', '맛집'), ('CAFE', '기본', '분위기');

-- 음식점 / 식당 (FOOD)
INSERT INTO keywords (industry_code, category, NAME) VALUES 
('FOOD', '기본', '맛집'), ('FOOD', '기본', '가성비'), ('FOOD', '기본', '감성'), 
('FOOD', '기본', '신메뉴'), ('FOOD', '기본', '분위기'), ('FOOD', '기본', '단체모임');

-- 미용 / 뷰티 (BEAUTY)
INSERT INTO keywords (industry_code, category, NAME) VALUES 
('BEAUTY', '서비스', '시술'), ('BEAUTY', '위생', '위생'), ('BEAUTY', '보안', '프라이버시'), 
('BEAUTY', '가격', '비용 합리성'), ('BEAUTY', '전문성', '상담 전문성'), ('BEAUTY', '맞춤', '고객맞춤'), 
('BEAUTY', '관리', '사후 관리'), ('BEAUTY', '트렌드', '트렌드 민감도'), ('BEAUTY', '신뢰', '평판');

-- 패션 / 의류 (FASHION)
INSERT INTO keywords (industry_code, category, NAME) VALUES 
('FASHION', '스타일', '스타일'), ('FASHION', '품질', '품질'), ('FASHION', '서비스', '서비스'), 
('FASHION', '접근성', '접근성'), ('FASHION', '배송', '배송 및 반품'), ('FASHION', '스타일링', '스타일링'), 
('FASHION', '신뢰', '평판');

-- 숙박 / 펜션 (STAY)
INSERT INTO keywords (industry_code, category, NAME) VALUES 
('STAY', '공간', '공간'), ('STAY', '위생', '청결'), ('STAY', '분위기', '분위기'), 
('STAY', '가격', '가성비'), ('STAY', '시설', '부대시설'), ('STAY', '조망', '조망권'), 
('STAY', '보안', '프라이버시'), ('STAY', '편의', '어메니티'), ('STAY', '서비스', '호스트 서비스');

-- 피트니스 / 스포츠 (SPORTS)
INSERT INTO keywords (industry_code, category, NAME) VALUES 
('SPORTS', '강의', '티칭'), ('SPORTS', '위생', '청결'), ('SPORTS', '분위기', '분위기'), 
('SPORTS', '이벤트', '이벤트'), ('SPORTS', '접근성', '접근 편의성'), ('SPORTS', '시설', '설비');

-- 교육 / 학원 (EDU)
INSERT INTO keywords (industry_code, category, NAME) VALUES 
('EDU', '역량', '강사진 역량'), ('EDU', '환경', '학습 환경'), ('EDU', '커리큘럼', '커리큘럼'), 
('EDU', '가격', '수강료'), ('EDU', '위치', '통학'), ('EDU', '신뢰', '신뢰도'), 
('EDU', '정보', '정보력'), ('EDU', '소통', '학부모 소통'), ('EDU', '분위기', '분위기');

-- 의료 / 병원 (MED)
INSERT INTO keywords (industry_code, category, NAME) VALUES 
('MED', '전문성', '전문성'), ('MED', '위생', '청결'), ('MED', '신뢰', '신뢰도'), 
('MED', '가격', '가성비'), ('MED', '위치', '위치/시간'), ('MED', '상담', '상담/구독');

-- 소매 / 쇼핑 (RETAIL)
INSERT INTO keywords (industry_code, category, NAME) VALUES 
('RETAIL', '품질', '품질'), ('RETAIL', '가격', '가성비'), ('RETAIL', '배송', '배송'), 
('RETAIL', '위생', '청결'), ('RETAIL', '이벤트', '프로모션'), ('RETAIL', '신뢰', '재고 신뢰도');

/* 3. DEFAULT (전체 키워드 중복 제거) 데이터 삽입 */
-- 모든 업종에 해당하지 않을 경우를 대비해 위 키워드들의 중복을 제거한 전체 목록을 'DEFAULT' 코드로 저장합니다.
INSERT INTO keywords (industry_code, category, NAME)
SELECT DISTINCT 'DEFAULT', '전체', NAME FROM keywords;