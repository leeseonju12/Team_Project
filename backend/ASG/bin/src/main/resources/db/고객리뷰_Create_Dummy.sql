## 고객 리뷰 저장 테이블
## feedback_source 로 테이블 변경

USE gather;
DROP TABLE IF EXISTS feedback_source;

CREATE TABLE feedback_source (
    source_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_name VARCHAR(100) NOT NULL,
    platform VARCHAR(30) NOT NULL COMMENT 'NAVER, KAKAO, GOOGLE, INSTAGRAM, FACEBOOK',
    original_text TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);


## 크롤링 된 실제 스타벅스 강남R점 리뷰
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Philosophizing', 'GOOGLE', '강남역 인근에서 가장 쾌적한 카페 중 한 곳인, 스타벅스 강남R점 !! 풍미 훌륭한 마이크로 블렌딩 원두와 다양한 디저트, 케이크 등을 마음껏 즐길 수 있는 멋진 카페 !!', '2026-03-12 09:29:45');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Kai Lee', 'GOOGLE', '강남역 카페 스타벅스 리저브 강남역에서 가장 가까운 스타벅스 리저브 매장. 점포가 크고 자리 간격이 좁은데도 별로 여유가 없을 정도로 사람이 많은 편. 주말이면 크리스마스 시즌 한정 메뉴들은 저녁 시간 전에 솔드아웃 되는 듯. 리저브 음료들은 확실히 보기에도 좋고 풍미도 괜찮은 것 같다.', '2026-03-12 09:29:45');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('seeun park', 'GOOGLE', '🌝장점 -R 점이라 확실히 메뉴 선택권이 넓다 -강남역에 바로 앞이라 엄청 가깝다 🌚불편한점 -화장실은 건물 안화장실을 써야해서 스타벅스를 나가야함 -항상 사람이많음 친구 기다리느라 스타벅스 R점에서 기다리는데 항상 사람이 많은곳이었는데 운좋게 자리가 나서 앉아서 기다렸어요~ 안쪽에는 1인이 노트북하기좋은자리도 있고 아', '2026-03-12 09:29:45');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Byungkyu SHIN', 'GOOGLE', '넓고 좌석배치도 좋음. 스타벅스 중에 좋은 환경을 가진 매장임. 단점은 화장실이 건물과 공동으로 쓰는데 더럽고 작고, 충전가능한 자리가 별로 없음.', '2026-03-12 09:29:45');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('김경실', 'GOOGLE', '맛점하고 친구 와 스타벅스 리저브 방문을 하였습니다  내가 원하는 원두와 커피추출방법을 직접 선택하는 재미가 있어요. 눈앞에서 직접 커피를 내려주는 모습을 보며 마시니 보통 카페에서 커피주문할 때 단순히 "아이스아메리카노요" 하며 마시는 커피와는 사뭇 다른 고급스럽고 분위기를 느끼며 좋은 커피를 마신다는 느낌이였습니다.', '2026-03-12 09:29:45');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Henry LEE', 'GOOGLE', '정오에 방문했는데 생각보다 한적했습니다. 리저브한정 메뉴를 즐길 수 있으며 제가 구매했을때는 서비스로 초콜릿 2피스를 증정하였습니다. 제가 주문한 메뉴는 아이스커피푸라푸치노였고 리저브 한정 메뉴입니다. 맛은 커피우유아이스크림과 비슷하고 뒷맛이 깔끔해 개인적으로 만족했습니다. 한가지 아쉬운 점은 화장실이 작고 매점 내', '2026-03-12 09:29:45');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Learner', 'GOOGLE', '매장이 굉장히 크고 테이블도 많고 쾌적한 분위기입니다. 강남역 1번 출구에 있어서 찾기도 쉽고 접근성도 좋습니다. 다만 지금 건물앞이 공사중이라서 약간 돌아서 입구로 들어가야 합니다.', '2026-03-12 09:29:45');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('백유진', 'GOOGLE', '직원이 친절합니다. 사람이 매우많아 좌석에 앉는것은 쉽지 않습니다.', '2026-03-12 09:29:45');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('abrooyal K', 'GOOGLE', '분위기는 매우 고급스럽고 차분해보이지만 여느 스타벅스 대형 점포처럼 내부는 말소리로 매우 시끄럽기 때문에 주의해야 한다', '2026-03-12 09:29:47');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('두리번두리번 (help.doc)', 'GOOGLE', '천장까지 디자인 되있는데 스타벅스 중에서도 손꼽힐정도로 이쁜 매장이에요. 또한 스타벅스 매장중에서도 손꼽힐정도로 넓은데 그 많은 자리가 항상 가득차있을정도로 인기매장이에요. 하지만 노트북 들고가서 공부하는건 포기하세요. 시끄럽기도 하고 테이블이 좁거나 충전을 못하거나 의자가 불편하거나 모든자리가 공부하긴 불편합니', '2026-03-12 09:29:47');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('노시경 TRAVEL', 'GOOGLE', '내부가 아주 넓고 천장의 목구조 등 인테리어가 힙합니다.', '2026-03-12 09:29:47');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('hi eb', 'GOOGLE', '리저브 매장이라그런지 매장 내부도 넓고 엄청 깨끗하네요. 직원분들도 친절하셨고, 커피 원두에대해서부터 설명도 자세하게 해주셨어요. 다른 스타벅스 매장보다 처음보는 굿즈도 종류가 많네요!!', '2026-03-12 09:29:47');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('WANGYOUNG LEE', 'GOOGLE', '서울에서 일일 유동인구가 가장 많은 강남역에서도 1번출구로 나와 10초컷! 2번출구로 나와도 20초컷에 도달할 수 있는 최고의 위치를 자랑하는 강남R점은 스타벅스 리저브중에서도 단층규모로는 최대 크기를 자랑하는 플래그쉽 매장입니다. ㄴ자 구조로 쭉뻗은 매장에서 리저브는 역ㄷ자 형태로 좌석이 다시 오각형태로 조금씩 각이져 있어', '2026-03-12 09:29:47');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Andrew Kim', 'GOOGLE', '강남역 대표 스타벅스 리저브카페로 모든게 훌륭해 보이지만 아쉬운 점 두 가지. • 매장 이용객수보다 테이블수가 적어 회전이 느림, 앉을자리 찾기가 힘든편 • 매장입장시 체온측정 or 방문등록등을 담당하는 직원이 없어 코로나19 위험에 노출되어 있는 편', '2026-03-12 09:29:47');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Kensa', 'GOOGLE', '인테리어 레전드 안에 작은 바처럼 구성해놓은것도 좋네요', '2026-03-12 09:29:47');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('홍지안', 'GOOGLE', '강남역 바로 앞 넓은 리저브 매장입니다', '2026-03-12 09:29:47');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('루나', 'GOOGLE', '매장 넓고 사람도 많고 테이블도 많고 뭐든 가득했어요', '2026-03-12 09:29:47');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('박유나', 'GOOGLE', '분위기도 좋구, 좋아하는 사람과 행복하고 여유롭게 힐링 했어요', '2026-03-12 09:29:47');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('앉으나서나', 'GOOGLE', '앉는자석 넘 불편해요. 오래앉아있지말라는 의지가보임ㅋㅋㅋㅋ 사람은또 많아요.', '2026-03-12 09:29:49');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('okin seon', 'GOOGLE', '리저브 음료 마시기에 좋고, 위치 좋고, 사람이 많아도 너무 시끄럽지 않아서 괜찮고 그렇네요!', '2026-03-12 09:29:49');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Minseong "U Seok" Kim', 'GOOGLE', '색다른 스타벅스 커피맛을 느낄 수 있어요! 대신 가격이 다른 스타벅스에 비해 비쌉니다. 잔에 6천원', '2026-03-12 09:29:49');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('musiqzz aj', 'GOOGLE', '강남R 강남역을 대표하는 Reserve 매장. 할로윈 컨셉으로 파트너들이 코스프레 하였지만 전혀 행복해 보이지 않는다. 이 많은 사람들을 상대하는데 고작 6명에서 일한다. 안타까울 정도이다. 어떤 파트너는 앉아서 한숨을 쉰다. 스타벅스 코리아는 반성해야한다. 한국의 전통적인 설과 추석. 정월대보름 단오 동짓달 같은 컨셉은 하나도 없으면', '2026-03-12 09:29:49');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Steven Kim', 'GOOGLE', '의외로 사람들도 별로 없고 넓어서 좋았어요', '2026-03-12 09:29:49');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('EunJu Yun', 'GOOGLE', '강남역2호선 1번출구 나오자마자 1분이내라 접근성좋고 매장도 넓고 창가쪽이 유리라 탁 트인 느낌여서 주말 아침에는 사람 많지 않아서 간단하게 아점먹고 노트북사용하기 좋음.', '2026-03-12 09:29:49');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('한지우', 'GOOGLE', '아아 시킬때 추천해주는 리저브 원두로 바꿔서 먹었는데 맛이 꽤 좋았습니다. 매장도 넓고 쾌적해요. 다만 화장실은 좀 청결하지 못합니다.', '2026-03-12 09:29:49');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('TED L', 'GOOGLE', '위치가 위치인지라 넓직하고 테이블도 넉넉하지만 그만큼 사람도 많네요. 그래도 많이 붐비진 않았습니다.', '2026-03-12 09:29:49');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Kidult DaN키덜트 댄', 'GOOGLE', '강남역 가면 항상 들리는 지점인데 항상 자리는 만석..ㅠㅠ 어제는 할로윈 분위기로 바껴서 서서 구경하고 왔습니다. ^^', '2026-03-12 09:29:49');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('BH Park', 'GOOGLE', '강남역 1번출구와 가깝고 매장이 진짜 크다. 그래서 사람도 엄청많아 자리잡는 것이 힘들다. 동네에 위치한 스타벅스와 비교하자면 더 복잡하고 좀 어수선 한 면도 있다. 그리고 주문과 음료를 받는 대기 시간이 길다. 그 만큼 사람이 많으니.....', '2026-03-12 09:29:49');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('UTO LEE', 'GOOGLE', '여느 스타벅스 못지않은 친절한 서비스. 천장이 높고 공간들이 트여있어서 쾌적한 것 같으면서도 통풍이 잘 이루어지지 않아 답답. md상품은 적은 편. 리저브드 커피를 좋아하면 다양한 방식으로 추출한 커피를 맛 볼 수 있다는 건 장점. + 아주 큰 단점 중 하나는, 콘센트가 너무 너무 적다는 점. 지금까지 가 본 스벅 중에 제일 적다.', '2026-03-12 09:29:51');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('독바위역.', 'GOOGLE', '여기..좀 골빈 분들이 좀 있으셨던 것 같아요 최소 15명은 앉을 수 있을 것 같은 자리에 두명이서 앉아놓고는 지들끼리 귓속말로 수근덕대면서 위아래로 훑더라구요..( 못생겼던데 ) 어쨌든 그런분들은 많았지만 여기에 계시는 바리스타 및 직원분들은 감사하게도 많이 친절하셨고 디저트도 여러가지라 색다르게 잘 먹고 왔던 것 같습니다.~', '2026-03-12 09:29:51');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Jh Seo', 'GOOGLE', '3개 선택.  맛이 너무진한 아이스크림어쩌구는. 못 먹었어요. 시그니처음료는 진리인듯.', '2026-03-12 09:29:51');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('유연', 'GOOGLE', '다른 스타벅스보다 넓고 깨끗합니다. 또 인테리어도 예뻐서 보는맛이 있는 카페!', '2026-03-12 09:29:51');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Eunjeong Kim', 'GOOGLE', '제 직장 1층이 스벅이라 자주 가는데 엄청 큰 곳임에도 불구하고 빈ㅈㅏ리가 없어요. 직원들도 친절 가격이 조금 비싸도 자꾸 가게 되는 곳', '2026-03-12 09:29:51');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Yonghwa Kwon', 'GOOGLE', '스타벅스 강남R점~! 강남역에서 제일 넓은 것 같습니다. 옛날에 강남 삼성AS센터였던 자리여서 내부공간은 바뀌었지만 나가는 곳이 똑같아서 추억돋았네요~^^ 넓고 탁트인 스벅에 가고싶으면 방문해보세요~^^', '2026-03-12 09:29:51');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Go4K ChiPro (Visbimmer0120)', 'GOOGLE', '다른 곳보다는 좀 더 특별한 스타벅스 매장. 국내 타매장에는 없는 혹은 보기힘든 메뉴들도 존재하며 바(bar)형태의 공간은 미국 캘리포니아 그리고 뉴욕에서도 본 적 없음. 제일 좋아하는 메뉴인 시큼하고 달콤한 패션후르트 아이스티도 한국에서 접할 수 있는 몇 안되는 곳. 공간은 큰편. 그러나 한국 스타벅스 매장들의 공간은 전반적으로', '2026-03-12 09:29:51');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('작은어복이', 'GOOGLE', '매장이 아주 크지만 점심시간즈음엔 사람이 붐벼 자리 찾기가 어려움. 블랙핑크케익.only Korea~', '2026-03-12 09:29:51');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Zion Choi', 'GOOGLE', '강남에 이렇게 넓은 스타벅스 매장이 있는 줄 이제 알았네요. 독특한 인테리어가 너무 멋진데 사람이 너무 많아서 자리잡기 힘들어요.', '2026-03-12 09:29:51');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('C Crew', 'GOOGLE', '강남역 바로 앞 스타벅스입니다. 사람이 많지만 그만큼 넓어서 쾌적해요. 강남역 주변 약속이 있으면 만나기 전에 좋을듯합니다.', '2026-03-12 09:29:51');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Gidae Yeo', 'GOOGLE', '넓고 쾌적하다. 스벅은 언제나.', '2026-03-12 09:29:53');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('너랑깊은인기보라 (우주속의별)', 'GOOGLE', '강남역1번출구  앞이라  좋았어요~시원한음료 와 실내가  넓어서  굿^^', '2026-03-12 09:29:53');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Jaewon Yun', 'GOOGLE', '사람 많은 만큼 넓기도 넓다 생각보다 옆자리와 간격이 좁아 주변 테이블 소음 넘 잘들림.... 그래도 좋았습니다', '2026-03-12 09:29:53');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('김지현', 'GOOGLE', '1일 1스타벅스 아깝지않다 여긴 갈때마다 느끼는디 바빠서 그런가 직원들 친절도가 타 스벅매장에비해 별로임...ㅠㅠ', '2026-03-12 09:29:53');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('김병관', 'GOOGLE', '쾌적하고 넓은 실내와 분위기가 좋습니다~~하지만 사람이 너무 몰려서 한가한 시간대를 이용해야 할 듯 싶습니다', '2026-03-12 09:29:53');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('나윤관', 'GOOGLE', '위치 좋은 스타벅스 리저브!!! 오늘도 새로운 원두 도전^^', '2026-03-12 09:29:53');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('태안', 'GOOGLE', '항상 바글바글한 곳 자리 잡기 어려워서 별 세개입니다 커피맛도 서비스도 좋아요', '2026-03-12 09:29:53');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('신명숙', 'GOOGLE', '명절연휴라 문연 카페가 별로 없었는데 휴일이라 샌드윗 없는건 아쉬웠지만 신상 단호박라떼 위드샷 과 함께 하나남은 맥시칸브리또로  점심까지 해결했어요^^', '2026-03-12 09:29:53');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Seunghye Hong', 'GOOGLE', '매우 편리한 위치, 넓고 쾌적한 공간 리저브 좌석이 별도로 있고 직원들이 매우 친절함.', '2026-03-12 09:29:53');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('finding true self', 'GOOGLE', '사람이 지나치게 느므느므느므 많아요.', '2026-03-12 09:29:53');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('전재필', 'GOOGLE', '스타벅스의 수준높은 커피를 맛볼 수 있는 곳..넓고 편안한 매장과 맛있는 커피 그리고 디저트들이 있음.', '2026-03-12 09:29:55');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('ANTON space', 'GOOGLE', '정말 바쁘고 정신없는 곳인데 직원분들 다 엄청 친절함. 커피도 맛있고 복잡한데 늦게나오지않음.', '2026-03-12 09:29:55');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('전준호', 'GOOGLE', '여느 스타벅스처럼 사람이 많다. 들어가는 입구에 비해 내부 공간이 넓다.', '2026-03-12 09:29:55');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Min Song Kim', 'GOOGLE', '항상 갈 때 마다 그 넓은 공간에 자리가 없음이 안타까운 곳.', '2026-03-12 09:29:55');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Suyoung Song', 'GOOGLE', '장소가 강남인 만큼 사람이 어마어마하게 많지만 그래도 있기에 좋은 공간입니다.', '2026-03-12 09:29:55');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('김하나', 'GOOGLE', '매장이 넓고 의자가 편해서 좋아용', '2026-03-12 09:29:55');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('신사동사막여우', 'GOOGLE', '매장도 넓고 음료도 다양해서 좋아요☕😊', '2026-03-12 09:29:55');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('지연sugar', 'GOOGLE', '매장 분위기 좋아요.. 넓직하고 천장 높아서 좋네요~', '2026-03-12 09:29:55');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Leon', 'GOOGLE', '넓고 좋습니다. 매장내부도 프리미엄', '2026-03-12 09:29:55');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Taegyun Kim', 'GOOGLE', '매장 넓고 깔끔합니다. 인터넷도 잘 터짐.', '2026-03-12 09:29:55');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('김종숙', 'GOOGLE', '사람들로 꽉차고 깨끗하지는 않음 유리 지저분하여 밖을 볼때~', '2026-03-12 09:29:57');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('오글이', 'GOOGLE', '역시 스벅은 생크림 가득 카스텔라가 짱입니다 바쁜신대도 음료 친절하게 주셔서 감사해요~', '2026-03-12 09:29:57');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Mijung미댕', 'GOOGLE', '여기 친절하고 너무 좋아요', '2026-03-12 09:29:57');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('별이', 'GOOGLE', '분위기 좋고 넓어요. 천장 고도가 높아서 답답함 없고 시간 보내기 좋아요. 다만 좌석이 다다닥 붙어있는 것이 좀 불편할 수 있습니다.', '2026-03-12 09:29:57');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Sana Hyon', 'GOOGLE', '강남역 1번 출구에 위치해 사람 만나기 좋은 곳 그러나 너무 큰 공간 때문에 공간 소음이 있어 저용하지는 않아요', '2026-03-12 09:29:57');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('정한별', 'GOOGLE', '리저브 커피와 자세한 커피설명 그리고 서비스 쬬꼬렛 한여름에도 대비되는 시원하다 못해 추운 에어컨', '2026-03-12 09:29:57');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('이혜숙', 'GOOGLE', '친절하고 깨끗한 조용한 분위기 좋아요', '2026-03-12 09:29:57');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('김미경', 'GOOGLE', '뷰도 좋고 사람도 많고 좋아요 ㅋㅋ자리 맡기 힘듦', '2026-03-12 09:29:57');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Roy Song', 'GOOGLE', '커피가 맛있어서..다만 비슷한 가격대 그 이하의 커피 전문점인 가배미학 등보다는 별로... 그래도 강남역 바로 앞에서 이 정도 커피면 뭐...', '2026-03-12 09:29:57');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('이명석 (과거와 오늘 jewap)', 'GOOGLE', '자리많다. 하지만 사람도 많다', '2026-03-12 09:29:57');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Guna Ryu', 'GOOGLE', '사람없을 오전에 가면 정말 좋은 곳', '2026-03-12 09:30:05');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('투투스명품시계매입.감정.수리', 'GOOGLE', '장점:강남역과 가깝고 매장규모가 커요 리저브 매장이며 의자 좌석이 편합니다 단점:W.C가 매장내에 없고 구입하는줄 과 음료타는 대기줄이 길어요 기타:직원분들 친절도와 서비스는 , 대기줄이 길어 직접 구입을하지 않고 사이렌오더로 구입하여 자세하게는 못느껴 보았지만 음료 받을때 느낌은 사람이 많다보니 기계적인 느낌 이였습니', '2026-03-12 09:30:05');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('ts lee', 'GOOGLE', '일단은 강남역1번출구를 바로 나오면 바로앞에 보이기 때문에 강남에서 약속이 있을때마다 이쪽을 찾아와서 기다리기도 하는데요. 다른 스타벅스 매장보다도 넓고 단층으로 확 트여있는 매장이라 개방감이 있어서 좋았습니다. 인테리어도 분위기가 아늑해요', '2026-03-12 09:30:05');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('사과는애플', 'GOOGLE', '역 앞에 있어서 신논현역에서 나오면 바로 찾을 수 있어요 지하에 교보문고 있어서 자주 들리는데 일반 스타벅스 매장과 인테리어가 살짝 다릅니다 리저브만 이용 할 수 있는 좌석?이 따로 있어요 그래서 바리스타분이 만드는 커피를 보는 재미도 있어요 갈 때 마다 다르긴 하지만 강남의 다른 스타벅스 매장들에 비해 조용한 편인 것 같아요', '2026-03-12 09:30:05');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Happy Smile', 'GOOGLE', '매장이 일단 널찍하고 쾌적합니다. 뷰가 딱히 좋을것도 없는데 좋아보이는 착각이 들 정도고 앉아서 차마시고 쉬기에 너무 좋은 장소입니다. 다만 평일 점심시간에는 인근 직장인들로 매우 붐비고 대기가 많은 편이긴 하니 참고하세요.', '2026-03-12 09:30:05');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('유재흠', 'GOOGLE', '다른 스타벅스 매장과 다른 고급진 느낌이 있다 리저버 메뉴의 커피가 상당히 맛있다', '2026-03-12 09:30:05');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('eunsil Lim', 'GOOGLE', '항상 갈때마다 친절하고 서비스가 좋아서 기분 좋아지는 지점이에요~ 매장도 크고 시원시원하게 오픈된 공간이라 분위기도 좋아요 할로윈 이벤트로 할로윈음료 구매시 코인뽑기 이벤트도 하고 (다른 매장에서는 못봤는데ㅠ 억울..ㅠ) 이벤트 기간동안 인테리어도 재미있게 꾸며져서 좋은 것 같아요', '2026-03-12 09:30:05');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('jinha choi', 'GOOGLE', '매장은 넓지만 사람이 미어 터져서 혼잡하고 주문 한다음 알람벨(?) 같은 기계를 구비해 두지 않아 일일히 직원이 불르는데 사람들 소리에 뭍혀서 잘 안들린다. 직원들 서비스는 좋음.', '2026-03-12 09:30:05');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('K N', 'GOOGLE', '사람 항상 많아요. 쇼파 자리 잡기는 좀 힘들지만. 맛은 스타벅스 맛이에요ㅋㅋㅋ 평타 이상! 역에서 나오면 바로 앞에 있어서 접근성 최고에요', '2026-03-12 09:30:05');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('김m.y.', 'GOOGLE', '강남역1번출구 바로 앞에 있어서 접근성이 매우 좋아요. 실내도 넓고 자리도 잘 되어 있지만 사람이 많아서 메뉴가 나올때까지 시간이 좀 걸립니다.', '2026-03-12 09:30:05');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('J Kim', 'GOOGLE', '리저브 매장 답게 크고 여유로운 공간, 커피맛도 좋음. 바리스타분들도 매우 친절. 다만 랩탑 콘센트를 찾아 앉아있는 매장 한 쪽 벽면 테이블 간격이 너무 좁고 프라이버시가 전혀 없음. 또 화장실 가기가 많이 불편함. 건물에 있는 화장실이 좁고 멀리 있으며 청결기준 별로임.', '2026-03-12 09:30:07');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('스토리텔러손 (PROMSON CEO)', 'GOOGLE', '강남역 출구에 바로 접근하기 좋은 위치에 있고 자리가 매우 넓지만, 앉을 곳이 잘 없는게 단점..', '2026-03-12 09:30:07');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('이진수', 'GOOGLE', '좌석 많고, 약간 반2층의 높이여서 창가 좌석이 나쁘지 않음. 강남역 나갈때 차는 거의 갖고 가지 않아서 주차는 잘 모르겠음. 위치가 상대적으로 외져서 접근성 떨어져서 사람 별로 없다고 생각됨.', '2026-03-12 09:30:07');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('eunhoi heo', 'GOOGLE', '강남역 1번 출구에서 나와 첫번째 우측 골목길에서 우회전하면 약 10~20m 우측에 정문이 있습니다. 비교적 넓은 면적에 테이블이 많고 지리적인 여건으로 이용자가 많은 편입니다. 이런 점을 감안하면 비교적 좋은 편이라 생각합니다. 자가 차량 이용하는 경우 주차비를 감안하면 되겠습니다.', '2026-03-12 09:30:07');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Jihye Brenda Cho', 'GOOGLE', '친구 동생이 부점장으로 있다고 해서 첫 발을 디디게 되었는데, 이게 왠일. 정말 넓고 사람도 물론 정말 많지만, 나름 정갈하게 잘 놓여있는 테이블들과 서로들 만나서 신나게 만담을 나누고있는모습들이 활기찬 곳이다. 출구 바로 앞이라 찾기도 너무 쉽고일단^^', '2026-03-12 09:30:07');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('어차칼로지', 'GOOGLE', '장소에 맞게 시간떼우기 딱이오나 자리 구하는데 오금이 저린다', '2026-03-12 09:30:07');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('선남선녀부부의 인생 2막', 'GOOGLE', '넓고 확트인 공간에 흐르는 열기가 똑같은 커피를 마셔도 더 맛나게 느껴지게 해줬습니다^^', '2026-03-12 09:30:07');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('꾸덕꾸덕 (쿠키다스)', 'GOOGLE', '강남역 2번과 1번사이에 위치하여 접근성이 좋음. 리저브 매장이라 그런가 지역색이 발현되서인가모르겠지만 아주 고급 스러운 인테리어. 어느시간에 가도 붐비는 편이나 매장도 넓고 자리도 테이블, 다인용 테이블, 리저브용 바 등 다양해서 앉을자리는 있는 편이다. 다만 테이블당 콘센트 수는 아주 적은 편이다 보통 스타벅스가 다인 테이', '2026-03-12 09:30:07');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('SJ HONG', 'GOOGLE', '넓고 쾌적하고 좋아요 :)', '2026-03-12 09:30:07');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('김창균', 'GOOGLE', '리저브 매장중엔 큰 편에 속한다. 콘센트 좌석은 많이 없기 때문에 간단한 작업을 하기엔 별로다. 협탁도 음료쟁반이 간신히 올라갈 만한 작은 좌석도 더러 있다', '2026-03-12 09:30:07');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('hn s', 'GOOGLE', '공간이 얿직하고 시원하게 동선이 짜여있어 좋습니다. 리져브 자리가 한가운데 있고 강남역이라서 다소 시끄럽지만 리져브 매장의 여유로움 같음이나 고급스러움을 느끼기에는 역시나 사람이 많은 강남역 매장 입니다.', '2026-03-12 09:30:10');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('킴진서비', 'GOOGLE', '강남역 1번출구 앞에 있음(입구 표시가 없어 난감함) 매장은 매우 넓고 사람도 많음(매우 시끄러움) 리저브 매장이어서 커피 원두도 선택 가능 커피맛은 일반 스벅과 비슷함 직원들은 친절함 다만 건물 화장실은 문이 오픈되어있어 부담스러움', '2026-03-12 09:30:10');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('나당', 'GOOGLE', '자주 가는곳입니다. 직원들은 친절한사람은 친절 가끔 무뚝뚝한 직원도 보여요 항상 사람이 많아 자라잡기가 참 어려운곳. 강남역 사거리에 통유리라 밤에도 도시 야경이 이쁩니다.', '2026-03-12 09:30:10');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('HY RAVEN', 'GOOGLE', '2인 테이블이 많음. 노트북사용가능한 좌석은 사실상 거의 차있는경우가많아요. 근처에 대부분 사무실이라 그런듯합니다.', '2026-03-12 09:30:10');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('김경남', 'GOOGLE', '파트너분들이 친절하고 미소 가득해서 인상깊었네요^^ 역시나 사람 많고 자리는 부족하며 시끄러움에도 불구하고, 아아 그란데를 기분좋게 즐기다 왔슴당', '2026-03-12 09:30:10');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('손무현', 'GOOGLE', '넓고 분위기 좋아요.', '2026-03-12 09:30:10');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('짱구와흰둥이', 'GOOGLE', '사람이 많기는 하지만 강남역이랑 가까워용 ㅎㅎ 화장실은 매장 나와야 함', '2026-03-12 09:30:10');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('Nyanya', 'GOOGLE', '강남에 스타벅스는 많지만.. 이 지점이 가장 역에서의 접근성이 좋은거 같다 리저브 매장이고 음식을 많이 판다. 건물 로비에 화장실이 있다. 건물은 1층으로 넓게 배치되어있지만 좌석이 그다지 합리적으로 배치 되어있지않아서 이용고객에 비해 좌석이 항상 애매한편이다.. 개인적으로는 좌석을 어떻게좀 해줬으면 좋겠는데 별로 변화는', '2026-03-12 09:30:10');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('하이웨이샌드위치', 'GOOGLE', '몬테소리점과 대각선 위치에 있는 스타벅스입니다. 몬테소리점에 비해서 매장 크기가 크지만 그만큼 이용자 수도 많아서 일찍 가지 않으면 자리를 찾기가 어렵습니다. 또한 윗층의 병원과 근처 회사에서 점심 시간 근처에 커피 사시러 많이 오셔서 정말 대기자 수가 많습니다. 그리고 매장 크기에 비해서 콘센트 위치가 많지는 않습니다. 매', '2026-03-12 09:30:10');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('opus 1', 'GOOGLE', '근처 스타벅스랑 달리 엄청 친절하고 커피 설명 물어봤을때 자세히 말해줌.', '2026-03-12 09:30:10');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('ddolai97', 'GOOGLE', '시장같은 리저브 분위기 ㅋ', '2026-03-12 09:30:12');
INSERT INTO feedback_source (author_name, platform, original_text, created_at) VALUES ('이승우', 'GOOGLE', '넓고 좋아요 평일 점심에도 사람은 많은데 매장이 넓어서 앉을 자리는 있습니다.', '2026-03-12 09:30:12');