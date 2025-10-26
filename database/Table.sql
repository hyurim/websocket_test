CREATE TABLE kanji (
  id          BIGSERIAL PRIMARY KEY,	-- 한자 ID
  glyph       TEXT NOT NULL UNIQUE,     -- 한자 (예: 学)
  kunyomi	  TEXT,						-- 훈독
  onyomi	  TEXT,						-- 음독
  meaning     TEXT NOT NULL,            -- 뜻
  reading VARCHAR(100) NOT NULL			-- 읽는 법 
);

---------------------------------
-- 훈독 단어
CREATE TABLE kanji_kunyomi (
  id          BIGSERIAL PRIMARY KEY,
  kanji_id    BIGINT NOT NULL REFERENCES kanji(id) ON DELETE CASCADE,
  kun_glyph   TEXT,                     -- 훈독 단어 한자 표기 
  kun_kana    TEXT,         			-- 훈독 단어 히라가나 
  kun_meaning	  TEXT 						-- 훈독 단어 뜻
);

---------------------------------

-- 음독 단어
CREATE TABLE kanji_onyomi (
  id          BIGSERIAL PRIMARY KEY,
  kanji_id    BIGINT NOT NULL REFERENCES kanji(id) ON DELETE CASCADE,
  on_glyph 	  TEXT,                     -- 음독 한자 표기 (예: 学ぶ, 学校)
  on_kana     TEXT,        				-- 음독 히라가나 (예: まなぶ, がく)
  on_meaning  TEXT 						-- 훈독 단어 뜻
);

---------------------------------

-- 훈독 문장 
CREATE TABLE kun_sentence (
  id            BIGSERIAL PRIMARY KEY,
  kanji_id    BIGINT NOT NULL REFERENCES kanji(id) ON DELETE CASCADE,
  kun_jp_text       TEXT,           		-- 일본어 원문
  kun_kr_text       TEXT                    -- 한국어 번역(선택)
);

---------------------------------

-- 음독 문장
CREATE TABLE on_sentence (
  id            BIGSERIAL PRIMARY KEY,
  kanji_id    BIGINT NOT NULL REFERENCES kanji(id) ON DELETE CASCADE,
  on_jp_text       TEXT,           		-- 일본어 원문
  on_kr_text       TEXT                    -- 한국어 번역(선택)
);

---------------------------------

-- 유저 테이블
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY, 					-- 내부 식별용
    login_id VARCHAR(50), 							-- 로그인용 아이디
    password VARCHAR(255), 							-- 비밀번호
    nickname VARCHAR(50), 							-- 닉네임
    streak_days INT DEFAULT 0, 						-- 연속으로 로그인한 날짜
    role VARCHAR(20), 								-- 유저 구분 (ADMIN은 직접 추가)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 생성 날짜
    last_login_at TIMESTAMP							-- 마지막 로그인 날짜
);

---------------------------------

-- JLPT 단어
CREATE TABLE jlpt_vocab (
    voca_id       BIGSERIAL PRIMARY KEY,    -- 고유 ID
    word          TEXT NOT NULL,            -- 단어 (예: 勉強)
    reading       TEXT,                     -- 읽기 (예: べんきょう)
    meaning_kr    TEXT,                     -- 한국어 뜻 (예: 공부)
    jlpt_level    VARCHAR(5) NOT NULL,      -- N1~N5
    part_of_speech VARCHAR(50),             -- 품사 (명사, 동사 등)
    example_jp    TEXT,                     -- 예문 (일본어)
    example_kr    TEXT                     -- 예문 번역
);

--------------------------------

-- 유저가 저장한 한자 
CREATE TABLE user_saved_kanji (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    kanji_id BIGINT REFERENCES kanji(id) ON DELETE CASCADE
);

-- 유저가 저장한 단어
CREATE TABLE user_saved_vocab (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    vocab_id BIGINT  REFERENCES jlpt_vocab(voca_id) ON DELETE CASCADE
);

---------------------------------

-- 채팅방 테이블
CREATE TABLE chat_room (
    id BIGSERIAL PRIMARY KEY,										-- 고유 ID
    name VARCHAR(100) NOT NULL,          							-- 채팅방 이름
    created_by BIGINT REFERENCES users(user_id) ON DELETE SET NULL,	-- 방을 만든 사용자
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP					-- 생성 날짜
);

-- 메시지 테이블
CREATE TABLE chat_message (
    id BIGSERIAL PRIMARY KEY,										-- 고유 ID
    room_id BIGINT REFERENCES chat_room(id) ON DELETE CASCADE,  	-- 메시지가 속한 채팅방 ID
    sender_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,  -- 보낸 유저
    message TEXT NOT NULL,                                        	-- 메시지 내용
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP					-- 메시지 전송 시간
);

-- 참여자 테이블
CREATE TABLE chat_participant (
    room_id BIGINT REFERENCES chat_room(id) ON DELETE CASCADE,		-- 참여중인 방의 ID
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,		-- 참여자의 ID
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,					-- 방에 참여한 시각
    PRIMARY KEY (room_id, user_id)									-- 한 유저가 같은 방에 중복 X
);


---------------------------------
---------------------------------

select * from kanji;
select * from kanji_onyomi;
select * from kanji_kunyomi;
select * from on_sentence;
select * from kun_sentence;
SELECT * FROM users;


 DROP TABLE users;
 DROP TABLE user_saved_kanji;
 
 DROP TABLE jlpt_vocab;
 DROP TABLE user_saved_vocab;