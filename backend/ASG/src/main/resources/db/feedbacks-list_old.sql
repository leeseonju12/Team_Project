/*
SQLyog Community v13.3.1 (64 bit)
MySQL - 10.4.32-MariaDB : Database - gather
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`gather` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;

USE `gather`;

/*Table structure for table `created_post` */

DROP TABLE IF EXISTS `created_post`;

CREATE TABLE `created_post` (
  `post_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '포스트 고유 번호',
  `user_id` bigint(20) NOT NULL COMMENT '작성자 고유 번호',
  `platform` varchar(20) NOT NULL COMMENT '포스팅 플랫폼 (INSTA, FB, BLOG 등)',
  `title` varchar(255) DEFAULT NULL COMMENT '포스트 제목',
  `content` text NOT NULL COMMENT '포스트 본문 내용',
  `image_url` varchar(500) DEFAULT NULL COMMENT '첨부 이미지 S3 또는 절대 경로',
  `original_link` varchar(500) DEFAULT NULL COMMENT '실제 업로드된 게시물 URL',
  `created_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT 'DB 생성 일시',
  `posted_at` datetime DEFAULT NULL COMMENT '실제 플랫폼 포스팅 일시',
  PRIMARY KEY (`post_id`),
  KEY `fk_post_user` (`user_id`),
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Data for the table `created_post` */

/*Table structure for table `customer_feedback` */

DROP TABLE IF EXISTS `customer_feedback`;

CREATE TABLE `customer_feedback` (
  `feedback_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `external_id` varchar(255) DEFAULT NULL,
  `author_name` varchar(255) DEFAULT NULL,
  `original_text` text DEFAULT NULL,
  `origin_url` varchar(255) DEFAULT NULL,
  `platform` varchar(50) DEFAULT NULL,
  `type` enum('COMMENT','REVIEW') DEFAULT NULL,
  `status` enum('CHECKED','COMPLETED','SENDING','UNCHECKED','UNRESOLVED') DEFAULT NULL,
  `ai_status` enum('DONE','IDLE') DEFAULT NULL,
  `ai_reply` text DEFAULT NULL,
  `sent_reply` text DEFAULT NULL,
  `source_id` bigint(20) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`feedback_id`),
  UNIQUE KEY `external_id` (`external_id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Data for the table `customer_feedback` */

insert  into `customer_feedback`(`feedback_id`,`external_id`,`author_name`,`original_text`,`origin_url`,`platform`,`type`,`status`,`ai_status`,`ai_reply`,`sent_reply`,`source_id`,`created_at`,`updated_at`) values 
(1,'17906277342388511','000707_7','안녕하세요 야로로님.','https://www.instagram.com/p/DWVSIFFDQRu/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE','안녕하세요! 야로로예요~ 오늘도 찾아주셔서 고마워요!','안녕하세요! 야로로예요~ 오늘도 찾아주셔서 고마워요!',NULL,'2026-03-27 23:02:21','2026-03-30 17:02:29'),
(2,'18078397052575609','yarrow.asg','맛있어보여요','https://www.instagram.com/p/DWVSIFFDQRu/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE','사진만 봐도 알아봐주시니 뿌듯해요! 맛도 자신 있으니 언제든 오세요!','사진만 봐도 알아봐주시니 뿌듯해요! 맛도 자신 있으니 언제든 오세요!',NULL,'2026-03-26 17:49:07','2026-03-30 17:02:29'),
(3,'17930410338226617','yarrow.asg','ㅁㄴㅇㄹ','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-20 09:52:53',NULL),
(4,'17889850950323758','yarrow.asg','ㅇㅅㅇ...','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-20 09:43:15',NULL),
(5,'18331646494217139','yarrow.asg','피곤해요','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-20 09:32:13',NULL),
(6,'18107743420837696','yarrow.asg','안녕하세요 오늘은 3월 20일이에요','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-20 09:24:24',NULL),
(7,'18132180997556244','yarrow.asg','ㅁㄹㅇㄴㅁㄹㅇㄴㅁㄹㄴㅁㄹㄴㅁㄹ','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-18 17:56:38',NULL),
(8,'18094769957085891','yarrow.asg','저희집 개가 좋아해요','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-18 17:43:19',NULL),
(9,'18039499367559074','yarrow.asg','그거아시나요 이미 mediaId로 commentId를 받고있었어요!','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-18 17:35:16',NULL),
(10,'18073837580444924','yarrow.asg','게시글 url 요청드려요^^','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-18 17:30:18',NULL),
(11,'18089362028468777','yarrow.asg','와정말요','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-18 17:29:06',NULL),
(12,'18097882544475987','yarrow.asg','테스트','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE','yarrow.asg님! \'테스트\' 감사해요! ? 다음엔 저희 맛으로 진짜 테스트하러 오세요! 기다릴게요!','yarrow.asg님! \'테스트\' 감사해요! ? 다음엔 저희 맛으로 진짜 테스트하러 오세요! 기다릴게요!',NULL,'2026-03-18 14:12:58','2026-03-30 17:02:29'),
(13,'17967768420034373','yarrow.asg','안녕하세요!','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE','yarrow.asg님, 안녕하세요! 먼저 인사 건네주셔서 참 반갑네요!? 언제든 편하게 찾아주세요!','yarrow.asg님, 안녕하세요! 먼저 인사 건네주셔서 참 반갑네요!? 언제든 편하게 찾아주세요!',NULL,'2026-03-18 10:13:21','2026-03-30 17:02:29'),
(14,'17851381605641387','yarrow.asg','대충 새로운 댓글','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-18 09:57:57',NULL),
(15,'18036348536779150','yarrow.asg','ㅇㅅㅇ','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE','yarrow.asg님! 무언가 딱 마음에 드셨기를 바라며! ? 또 오실 거죠?','yarrow.asg님! 무언가 딱 마음에 드셨기를 바라며! ? 또 오실 거죠?',NULL,'2026-03-17 11:22:13','2026-03-30 17:02:29'),
(16,'17909310939181000','yarrow.asg','43554365','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE','yarrow.asg님! 43554365! 혹시 다음주 로또 번호일까요? ? 저희 가게 들러 맛있는 식사와 행운도 같이 받아가세요! 늘 환영합니다~','yarrow.asg님! 43554365! 혹시 다음주 로또 번호일까요? ? 저희 가게 들러 맛있는 식사와 행운도 같이 받아가세요! 늘 환영합니다~',NULL,'2026-03-17 10:30:48','2026-03-30 17:02:29'),
(17,'18105905260848097','yarrow.asg','햄버거 맛있겠다','https://www.instagram.com/p/DV8C--rDwc-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE','yarrow.asg님! 다음엔 꼭 \"맛있다!\"로 바꿔드릴게요! 곧 만나요!??','yarrow.asg님! 다음엔 꼭 \"맛있다!\"로 바꿔드릴게요! 곧 만나요!??',NULL,'2026-03-16 17:35:29','2026-03-30 17:02:29'),
(18,'18071374505233088','yarrow.asg','안녕하세요 새로운 댓글이에요. 잘 보고 있어요','https://www.instagram.com/p/DV7z_sQj-aK/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-18 09:47:10',NULL),
(19,'18095859968514270','yarrow.asg','1234','https://www.instagram.com/p/DV7z_sQj-aK/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-17 10:31:10',NULL),
(20,'18100966886508688','yarrow.asg','5678','https://www.instagram.com/p/DV7zBqwDMg-/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-17 10:31:15',NULL),
(21,'18134764195509598','yarrow.asg','서울 도시철도','https://www.instagram.com/p/DV7ya-gDUVl/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-17 10:31:23',NULL),
(22,'18123344575587180','yarrow.asg','asdf','https://www.instagram.com/reel/DV7xg7VES2B/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE','yarrow.asg님, 찾아주셔서 고맙습니다! 또 들러주세요!','yarrow.asg님, 찾아주셔서 고맙습니다! 또 들러주세요!',NULL,'2026-03-24 16:21:50','2026-03-30 17:02:29'),
(23,'122104390653022910_2309458866216982','All Social Gather 소셜다모아','페이스북 테스트','https://www.facebook.com/1055275447664973_122104390653022910','FACEBOOK','COMMENT','COMPLETED','DONE','테스트도 소중하죠! 직접 오시면 더 반겨드릴게요?','테스트도 소중하죠! 직접 오시면 더 반겨드릴게요?',NULL,'2026-03-31 10:34:01',NULL),
(24,'122104390653022910_882505518129197','All Social Gather 소셜다모아','접시가 마음에 들어요','https://www.facebook.com/1055275447664973_122104390653022910','FACEBOOK','COMMENT','COMPLETED','DONE','소셜다모아님, 저희 접시 마음에 드셨군요! 예쁘게 봐주셔서 감사해요. 다음에 또 들러주세요!','소셜다모아님, 저희 접시 마음에 드셨군요! 예쁘게 봐주셔서 감사해요. 다음에 또 들러주세요!',NULL,'2026-03-31 09:43:38',NULL),
(25,'122104390653022910_1299957992075008','Unknown','게시글!! 댓글!!','https://www.facebook.com/1055275447664973_122104390653022910','FACEBOOK','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-31 10:40:50',NULL),
(26,'122104390653022910_1648824699468115','윤서희','트러플 파스타 맛있어보여요','https://www.facebook.com/1055275447664973_122104390653022910','FACEBOOK','COMMENT','COMPLETED','DONE','윤서희님, 저희 트러플 파스타 예쁘고 맛도 좋답니다! 다음엔 꼭 드시러 오세요!','윤서희님, 저희 트러플 파스타 예쁘고 맛도 좋답니다! 다음엔 꼭 드시러 오세요!',NULL,'2026-03-30 17:06:58',NULL),
(27,'122098132203022910_727299390377438','Unknown','나 안아!!','https://www.facebook.com/1055275447664973_122098132203022910','FACEBOOK','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-31 10:40:23',NULL),
(28,'122104390653022910_2348474925635728','윤서희','다음에 파스타 먹으러 갈게요!','https://www.facebook.com/1055275447664973_122104390653022910','FACEBOOK','COMMENT','COMPLETED','DONE','윤서희님! 네네, 다음에 파스타 꼭 드시러 오세요! 기다릴게요!','윤서희님! 네네, 다음에 파스타 꼭 드시러 오세요! 기다릴게요!',NULL,'2026-03-31 11:54:55',NULL),
(29,'122104390653022910_910521981808519','윤서희','혹시 주류 콜키지도 될까요?','https://www.facebook.com/1055275447664973_122104390653022910','FACEBOOK','COMMENT','COMPLETED','DONE','윤서희님! 안타깝지만 주류 콜키지는 어렵습니다. 양해 부탁드려요 ?‍♀️','윤서희님! 안타깝지만 주류 콜키지는 어렵습니다. 양해 부탁드려요 ?‍♀️',NULL,'2026-03-31 12:02:55',NULL),
(30,'122104390653022910_2357157828119213','윤서희','혹시 콜키지도 되나요?','https://www.facebook.com/1055275447664973_122104390653022910','FACEBOOK','COMMENT','COMPLETED','DONE','윤서희님, 콜키지는 아쉽게도 어렵습니다 ㅠㅠ 맛있는 음료 준비해드릴게요!','윤서희님, 콜키지는 아쉽게도 어렵습니다 ㅠㅠ 맛있는 음료 준비해드릴게요!',NULL,'2026-03-31 12:02:01',NULL),
(32,'18153184882455176','yarrow.asg','요즘 새로운 피드가 없네요!','https://www.instagram.com/p/DWVSIFFDQRu/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-03-31 16:34:01',NULL),
(33,'122104390653022910_872928752441632','윤서희','토마토 파스타도 나오면 좋겠어요','https://www.facebook.com/1055275447664973_122104390653022910','FACEBOOK','COMMENT','UNRESOLVED','DONE','윤서희님! 토마토 파스타! 오, 저도 생각해본 적 있는데! 좋은데요!',NULL,NULL,'2026-03-31 16:34:18',NULL),
(34,'122104390653022910_27432131139720477','윤서희','예약 가능한가요?','https://www.facebook.com/1055275447664973_122104390653022910','FACEBOOK','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-04-02 09:22:18',NULL),
(35,'17938140252080107','yarrow.dev','닥터페퍼 맛있겠당. 이거 끝나고 사먹으러 가야지','https://www.instagram.com/p/DWSewKeklAL/','INSTAGRAM','COMMENT','UNRESOLVED','IDLE',NULL,NULL,NULL,'2026-04-02 09:34:09',NULL);

/*Table structure for table `feedback_source` */

DROP TABLE IF EXISTS `feedback_source`;

CREATE TABLE `feedback_source` (
  `source_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `author_name` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `original_text` text DEFAULT NULL,
  `platform` enum('FACEBOOK','GOOGLE','INSTAGRAM','KAKAO','NAVER') DEFAULT NULL,
  `external_id` varchar(255) DEFAULT NULL,
  `origin_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`source_id`),
  UNIQUE KEY `uk_external_id` (`external_id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Data for the table `feedback_source` */

insert  into `feedback_source`(`source_id`,`author_name`,`created_at`,`original_text`,`platform`,`external_id`,`origin_url`) values 
(1,'yarrow.asg',NULL,'43554365','INSTAGRAM','1234',NULL),
(2,'yarrow.asg',NULL,'햄버거 맛있겠다','INSTAGRAM',NULL,NULL),
(3,'yarrow.asg',NULL,'43554365','INSTAGRAM','17909310939181000',NULL),
(4,'yarrow.asg',NULL,'햄버거 맛있겠다','INSTAGRAM','18105905260848097',NULL),
(5,'yarrow.asg',NULL,'ㅇㅅㅇ','INSTAGRAM','18036348536779150',NULL),
(29,'yarrow.asg',NULL,'안녕하세요!','INSTAGRAM','17967768420034373',NULL),
(30,'yarrow.asg',NULL,'대충 새로운 댓글','INSTAGRAM','17851381605641387',NULL),
(31,'yarrow.asg',NULL,'안녕하세요 새로운 댓글이에요. 잘 보고 있어요','INSTAGRAM','18071374505233088',NULL),
(32,'yarrow.asg',NULL,'1234','INSTAGRAM','18095859968514270',NULL),
(33,'yarrow.asg',NULL,'5678','INSTAGRAM','18100966886508688',NULL),
(34,'yarrow.asg',NULL,'서울 도시철도','INSTAGRAM','18134764195509598',NULL),
(35,'yarrow.asg',NULL,'테스트','INSTAGRAM','18097882544475987',NULL),
(36,'yarrow.asg',NULL,'와정말요','INSTAGRAM','18089362028468777',NULL),
(37,'yarrow.asg',NULL,'게시글 url 요청드려요^^','INSTAGRAM','18073837580444924',NULL),
(38,'yarrow.asg',NULL,'그거아시나요 이미 mediaId로 commentId를 받고있었어요!','INSTAGRAM','18039499367559074',NULL),
(39,'yarrow.asg',NULL,'저희집 개가 좋아해요','INSTAGRAM','18094769957085891',NULL),
(40,'yarrow.asg',NULL,'ㅁㄹㅇㄴㅁㄹㅇㄴㅁㄹㄴㅁㄹㄴㅁㄹ','INSTAGRAM','18132180997556244',NULL),
(41,'yarrow.asg',NULL,'안녕하세요 오늘은 3월 20일이에요','INSTAGRAM','18107743420837696',NULL),
(42,'yarrow.asg',NULL,'피곤해요','INSTAGRAM','18331646494217139',NULL),
(43,'yarrow.asg',NULL,'ㅇㅅㅇ...','INSTAGRAM','17889850950323758','https://www.instagram.com/p/DV8C--rDwc-/'),
(44,'yarrow.asg',NULL,'ㅁㄴㅇㄹ','INSTAGRAM','17930410338226617','https://www.instagram.com/p/DV8C--rDwc-/'),
(45,'yarrow.asg',NULL,'asdf','INSTAGRAM','18123344575587180','https://www.instagram.com/reel/DV7xg7VES2B/'),
(46,'yarrow.asg',NULL,'맛있어보여요','INSTAGRAM','18078397052575609','https://www.instagram.com/p/DWVSIFFDQRu/'),
(47,'000707_7',NULL,'안녕하세요 야로로님.','INSTAGRAM','17906277342388511','https://www.instagram.com/p/DWVSIFFDQRu/');

/*Table structure for table `guide_message_pool` */

DROP TABLE IF EXISTS `guide_message_pool`;

CREATE TABLE `guide_message_pool` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_id` bigint(20) DEFAULT NULL COMMENT '상위 가이드 FK',
  `content` text NOT NULL COMMENT '실제 가이드 문구 내용',
  PRIMARY KEY (`id`),
  KEY `fk_guide_config` (`config_id`),
  CONSTRAINT `fk_guide_config` FOREIGN KEY (`config_id`) REFERENCES `upload_guide_configs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Data for the table `guide_message_pool` */

/*Table structure for table `industry_keywords` */

DROP TABLE IF EXISTS `industry_keywords`;

CREATE TABLE `industry_keywords` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `industry_code` varchar(10) NOT NULL,
  `category` varchar(50) NOT NULL,
  `keyword1` varchar(50) DEFAULT NULL,
  `keyword2` varchar(50) DEFAULT NULL,
  `keyword3` varchar(50) DEFAULT NULL,
  `keyword4` varchar(50) DEFAULT NULL,
  `keyword5` varchar(50) DEFAULT NULL,
  `keyword6` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Data for the table `industry_keywords` */

insert  into `industry_keywords`(`id`,`industry_code`,`category`,`keyword1`,`keyword2`,`keyword3`,`keyword4`,`keyword5`,`keyword6`) values 
(1,'IND001','카페 / 베이커리','분위기','가성비','신메뉴','위치','트렌드','맛집'),
(2,'IND002','음식점 / 식당','분위기','가성비','맛집','위치','청결','모임장소'),
(3,'IND003','미용 / 뷰티','프라이버시','비용 합리성','전문성','사후 관리','트렌드','위생'),
(4,'IND004','패션 / 의류','스타일','품질','청결','위치 접근성','퍼스널 쇼퍼','평판'),
(5,'IND005','숙박 / 펜션','공간','청결','위치','가성비','부대시설','호스트 서비스'),
(6,'IND006','피트니스 / 스포츠','티칭','청결','분위기','프로모션','접근 편의성','설비'),
(7,'IND007','교육 / 학원','강사진 역량','학습 환경','커리큘럼','수강료','정보력','신뢰도'),
(8,'IND008','의료 / 병원','전문성','위생','신뢰도','위치/시간','진료 환경','소통'),
(9,'IND009','소매 / 쇼핑','품질','가성비','배송','청결','프로모션','재고 신뢰도'),
(10,'IND010','기타','공간 경험','평판','큐레이션','품질','가성비','청결');

/*Table structure for table `platform` */

DROP TABLE IF EXISTS `platform`;

CREATE TABLE `platform` (
  `platform_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '플랫폼 ID',
  `platform_code` varchar(30) NOT NULL COMMENT 'instagram, facebook, naver, google, kakao',
  `platform_name` varchar(50) NOT NULL COMMENT '플랫폼명',
  `brand_color` varchar(20) DEFAULT NULL COMMENT '차트/배지 색상 코드',
  `is_active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '사용 여부',
  PRIMARY KEY (`platform_id`),
  UNIQUE KEY `platform_code` (`platform_code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='플랫폼 마스터';

/*Data for the table `platform` */

insert  into `platform`(`platform_id`,`platform_code`,`platform_name`,`brand_color`,`is_active`) values 
(1,'instagram','인스타그램','#E1306C',1),
(2,'facebook','페이스북','#1877F2',1),
(3,'naver','네이버','#03C75A',1),
(4,'google','구글','#EA4335',1),
(5,'kakao','카카오','#FEE500',1);

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `addr_detail` varchar(100) DEFAULT NULL,
  `business_category` varchar(50) DEFAULT NULL,
  `company_name` varchar(100) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `event_consent` bit(1) DEFAULT NULL,
  `location_agreed` bit(1) NOT NULL,
  `marketing_consent` bit(1) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `preferred_channel` varchar(20) DEFAULT NULL,
  `privacy_agreed` bit(1) NOT NULL,
  `provider` varchar(20) NOT NULL,
  `provider_id` varchar(255) NOT NULL,
  `road_addr_part1` varchar(200) DEFAULT NULL,
  `role` varchar(20) NOT NULL,
  `signup_completed` tinyint(4) NOT NULL,
  `status` enum('ACTIVE','SIGNUP_PENDING') NOT NULL,
  `store_phone_number` varchar(20) DEFAULT NULL,
  `terms_agreed` bit(1) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKcbysvpk95086ud4n4g6mkspai` (`provider`,`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Data for the table `users` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
