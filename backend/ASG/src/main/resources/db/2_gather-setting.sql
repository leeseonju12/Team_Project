/*
  2_gather-setting.sql
  카페 디아즈 / 카페 산정 — 회원가입 완료 상태 로그인 더미
  실행 전제: 0_gather-setting.sql → 1_gather-setting.sql 실행 완료

  ══════════════════════════════════════════════════════════════════
  전체 실행 순서 (이 순서 반드시 지킬 것)
  ══════════════════════════════════════════════════════════════════

  STEP 1. 0_gather-setting.sql 실행   ← DB 스키마 생성
  STEP 2. 1_gather-setting.sql 실행   ← 채널 성과 더미 (brand.user_id = NULL 상태)
  STEP 3. 2_gather-setting.sql 실행   ← users 더미 + brand.user_id 연결
           (이 시점에서 users.id = 1, 2 가 INSERT 됨)

  STEP 4. 구글 계정 A 로 소셜 로그인 시도
           → /login 또는 /login_test 에서 Google 로그인 버튼 클릭
           → 회원가입 절차(약관·정보 입력) 진행
           → 완료 후 /dashboard 진입 확인
           → 이 시점에서 users 테이블에 id=3 (또는 그 이후) 행이 새로 생성됨

  STEP 5. 로그인으로 생성된 실제 users 행 확인
           SELECT id, provider, provider_id, email
           FROM users
           ORDER BY id DESC LIMIT 5;

           → 방금 로그인한 계정의 id 와 provider_id 값을 메모

  STEP 6. 카페 디아즈(brand_id=1) 에 실제 계정 연결
           -- ① users 더미 행(id=1) 의 provider_id 를 실제 값으로 교체
           UPDATE users
           SET provider_id = '여기에_5번에서_확인한_provider_id'
           WHERE id = 1;

           -- ② 또는, 새로 생긴 users 행(id=3 등) 을 brand 에 직접 연결하는 방법
           UPDATE brand SET user_id = 3 WHERE brand_id = 1;
           UPDATE users
           SET status = 'ACTIVE', signup_completed = 1
           WHERE id = 3;

           둘 중 하나만 실행하면 됩니다.
           ① 번이 더 간단하고 권장하는 방법입니다.

  STEP 7. 구글 계정 B 로 소셜 로그인 → 같은 방식으로 카페 산정(brand_id=2) 연결
           UPDATE users
           SET provider_id = '여기에_B계정_provider_id'
           WHERE id = 2;

  STEP 8. 각 계정으로 다시 로그인
           → 카페 디아즈 계정: 로그인 시 brand_id=1 데이터 조회됨
           → 카페 산정 계정:   로그인 시 brand_id=2 데이터 조회됨

  ══════════════════════════════════════════════════════════════════
  최종 연결 구조
  ══════════════════════════════════════════════════════════════════
  users.id=1 (provider=google)  →  brand.brand_id=1  →  채널성과 데이터 (디아즈)
  users.id=2 (provider=google)  →  brand.brand_id=2  →  채널성과 데이터 (산정)
  ══════════════════════════════════════════════════════════════════
*/

USE gather;
SET FOREIGN_KEY_CHECKS = 0;

-- ══════════════════════════════════════════════
-- 1. users
-- signup_completed=1, status=ACTIVE → 로그인 즉시 /dashboard 진입
-- ══════════════════════════════════════════════

-- 카페 디아즈 (카카오 로그인)
INSERT INTO users (
  id, email, name, nickname, provider, provider_id,
  contact_phone, company_name, business_category,
  preferred_channel, store_phone_number,
  road_addr_part1, addr_detail,
  terms_agreed, privacy_agreed, location_agreed,
  marketing_consent, event_consent,
  signup_completed, status, role,
  created_at, updated_at
) VALUES (
  1,
  'cafediaz@gmail.com',
  '카페디아즈',
  '카페디아즈',
  'google',
  'diaz_google_0000000001',  -- ← STEP 6에서 실제 provider_id로 UPDATE
  '01012341234',
  '카페 디아즈',
  '카페 / 베이커리',
  'INSTAGRAM',
  '0423226868',
  '대전 중구 은행동 157-2',
  '1층',
  1, 1, 1,
  1, 1,
  1, 'ACTIVE', 'ROLE_USER',
  '2026-01-10 09:00:00', '2026-03-01 09:00:00'
);

-- 카페 산정 (구글 로그인)
INSERT INTO users (
  id, email, name, nickname, provider, provider_id,
  contact_phone, company_name, business_category,
  preferred_channel, store_phone_number,
  road_addr_part1, addr_detail,
  terms_agreed, privacy_agreed, location_agreed,
  marketing_consent, event_consent,
  signup_completed, status, role,
  created_at, updated_at
) VALUES (
  2,
  'cafesanjeong@gmail.com',
  '카페산정',
  '카페산정',
  'google',
  'sanjeong_google_0000000002',  -- ← STEP 7에서 실제 provider_id로 UPDATE
  '01056785678',
  '카페 산정',
  '카페 / 베이커리',
  'INSTAGRAM',
  '0427241234',
  '대전 유성구 산정로 112',
  '1층',
  1, 1, 1,
  0, 0,
  1, 'ACTIVE', 'ROLE_USER',
  '2026-01-15 09:00:00', '2026-03-01 09:00:00'
);


-- ══════════════════════════════════════════════
-- 2. brand.user_id 연결
-- 1_gather-setting.sql 에서 user_id=NULL 로 생성된 brand 행을 업데이트
-- ══════════════════════════════════════════════

UPDATE brand SET user_id = 1 WHERE brand_id = 1;  -- 카페 디아즈
UPDATE brand SET user_id = 2 WHERE brand_id = 2;  -- 카페 산정


-- ══════════════════════════════════════════════
-- 3. business_hours (0=월 ~ 6=일)
-- ══════════════════════════════════════════════

-- 카페 디아즈: 화요일 정기휴무 (brand_operation_profile.regular_closed_weekday=2)
INSERT INTO business_hours (user_id, day_of_week, is_open, open_time, close_time) VALUES
(1, 0, 1, '09:00', '22:00'),  -- 월
(1, 1, 0, NULL,    NULL   ),  -- 화 (정기휴무)
(1, 2, 1, '09:00', '22:00'),  -- 수
(1, 3, 1, '09:00', '22:00'),  -- 목
(1, 4, 1, '09:00', '22:00'),  -- 금
(1, 5, 1, '09:00', '22:00'),  -- 토
(1, 6, 1, '09:00', '22:00');  -- 일

-- 카페 산정: 수요일 정기휴무 (brand_operation_profile.regular_closed_weekday=3)
INSERT INTO business_hours (user_id, day_of_week, is_open, open_time, close_time) VALUES
(2, 0, 1, '10:00', '21:00'),  -- 월
(2, 1, 1, '10:00', '21:00'),  -- 화
(2, 2, 0, NULL,    NULL   ),  -- 수 (정기휴무)
(2, 3, 1, '10:00', '21:00'),  -- 목
(2, 4, 1, '10:00', '21:00'),  -- 금
(2, 5, 1, '10:00', '21:00'),  -- 토
(2, 6, 1, '10:00', '21:00');  -- 일


-- ══════════════════════════════════════════════
-- 4. content_settings
-- ══════════════════════════════════════════════

-- 카페 디아즈: 인스타+카카오 중심, 친근한 톤
INSERT INTO content_settings (
  user_id, intro_template, outro_template,
  tone, emoji_level, target_length, preferred_sns
) VALUES (
  1,
  '안녕하세요, 대전 은행동 카페 디아즈입니다 ☕',
  '오늘도 디아즈에서 행복한 시간 보내세요 🌸',
  '친근한', '적당히', 300,
  'instagram,kakao,naver'
);

-- 카페 산정: 인스타+네이버 중심, 기본 톤
INSERT INTO content_settings (
  user_id, intro_template, outro_template,
  tone, emoji_level, target_length, preferred_sns
) VALUES (
  2,
  '안녕하세요, 대전 유성구 카페 산정입니다 :)',
  '방문해 주셔서 감사합니다. 또 오세요.',
  '기본', '적게', 250,
  'instagram,naver'
);


-- ══════════════════════════════════════════════
-- 5. inquiry (마이페이지 문의 탭 확인용)
-- ══════════════════════════════════════════════

-- 카페 디아즈 문의
INSERT INTO inquiry (type, email, title, body, content, status, created_at) VALUES
('가입·연동',  'cafediaz@gmail.com',
 '인스타그램 연동 후 게시물 업로드가 되지 않아요',
 '인스타그램 연동은 완료됐는데 게시물 업로드 버튼을 누르면 아무 반응이 없습니다.',
 '인스타그램 연동은 완료됐는데 게시물 업로드 버튼을 누르면 아무 반응이 없습니다.',
 '처리완료', '2026-03-05 10:22:00'),
('콘텐츠 생성', 'cafediaz@gmail.com',
 'AI 콘텐츠 생성 시 업종이 반영이 안 됩니다',
 '카페 업종으로 설정했는데 생성된 내용이 식당 느낌으로 나옵니다.',
 '카페 업종으로 설정했는데 생성된 내용이 식당 느낌으로 나옵니다.',
 '처리중', '2026-03-18 14:35:00'),
('시스템오류',  'cafediaz@gmail.com',
 '채널 성과 분석 페이지가 로딩이 안 돼요',
 '채널 성과 분석 메뉴 클릭 시 흰 화면만 나타납니다. 다른 메뉴는 정상입니다.',
 '채널 성과 분석 메뉴 클릭 시 흰 화면만 나타납니다. 다른 메뉴는 정상입니다.',
 '미처리', '2026-03-28 09:11:00');

-- 카페 산정 문의
INSERT INTO inquiry (type, email, title, body, content, status, created_at) VALUES
('가입·연동',  'cafesanjeong@gmail.com',
 '네이버 블로그 연동이 계속 실패합니다',
 '네이버로 로그인 후 블로그 연동을 시도하면 "연동에 실패했습니다" 오류가 납니다.',
 '네이버로 로그인 후 블로그 연동을 시도하면 "연동에 실패했습니다" 오류가 납니다.',
 '미처리', '2026-03-12 11:40:00'),
('계정',       'cafesanjeong@gmail.com',
 '가게 정보 수정 후 저장이 안 됩니다',
 '마이페이지에서 가게 주소를 수정하고 저장 버튼을 눌러도 변경이 안 됩니다.',
 '마이페이지에서 가게 주소를 수정하고 저장 버튼을 눌러도 변경이 안 됩니다.',
 '처리완료', '2026-03-20 16:05:00');


SET FOREIGN_KEY_CHECKS = 1;
