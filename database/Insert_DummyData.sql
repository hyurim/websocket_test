-- 한자
INSERT INTO kanji (glyph, kunyomi, onyomi, meaning, reading) VALUES
('学', 'まなぶ', 'がく, がっ', '배우다, 학문', '학'),
('火', 'ひ', 'か', '불', '火'),
('水', 'みず', 'すい', '물', '水'),
('木', 'き', 'もく, ぼく', '나무', '木'),
('山', 'やま', 'さん', '산', '山');

select * from kanji;

INSERT INTO kanji (glyph, meaning, reading, kunyomi, onyomi)
VALUES ( '人', '사람', '인', 'ひと', 'じん, にん');
---------------------------------
-- 훈독 단어
INSERT INTO kanji_kunyomi (kanji_id, kun_glyph, kun_kana, kun_meaning)
VALUES
  ((SELECT id FROM kanji WHERE glyph='学'), '学ぶ', 'まなぶ', '배우다'),
  ((SELECT id FROM kanji WHERE glyph='火'), '火', 'ひ', '불'),
  ((SELECT id FROM kanji WHERE glyph='水'), '水', 'みず', '물'),
  ((SELECT id FROM kanji WHERE glyph='木'), '木', 'き', '나무'),
  ((SELECT id FROM kanji WHERE glyph='山'), '山', 'やま', '산');

INSERT INTO kanji_kunyomi (id, kanji_id, kun_glyph, kun_kana, kun_meaning)
VALUES 
(6, 6, '人々', 'ひとびと', '사람들');

---------------------------------

-- 음독 단어
INSERT INTO kanji_onyomi (kanji_id, on_glyph, on_kana, on_meaning)
VALUES
  ((SELECT id FROM kanji WHERE glyph='学'), '学校', 'がっこう', '학교'),
  ((SELECT id FROM kanji WHERE glyph='火'), '火山', 'かざん', '화산'),
  ((SELECT id FROM kanji WHERE glyph='水'), '水道', 'すいどう', '수도'),
  ((SELECT id FROM kanji WHERE glyph='木'), '木曜日', 'もくようび', '목요일'),
  ((SELECT id FROM kanji WHERE glyph='山'), '富士山', 'ふじさん', '후지산');

INSERT INTO kanji_onyomi (id, kanji_id, on_glyph, on_kana, on_meaning)
VALUES 
(6, 6, '人口', 'じんこう', '인구');
---------------------------------

-- 훈독 문장 
INSERT INTO kun_sentence (kanji_id, kun_jp_text, kun_kr_text)
VALUES
  ((SELECT id FROM kanji WHERE glyph='学'), '私は日本語を学ぶ。', '나는 일본어를 배운다.'),
  ((SELECT id FROM kanji WHERE glyph='火'), '火を消してください。', '불을 꺼주세요.'),
  ((SELECT id FROM kanji WHERE glyph='水'), '水を一杯ください。', '물 한 잔 주세요.'),
  ((SELECT id FROM kanji WHERE glyph='木'), '木の下で休みましょう。', '나무 아래에서 쉽시다.'),
  ((SELECT id FROM kanji WHERE glyph='山'), '山に登るのが好きです。', '산에 오르는 것을 좋아합니다.');

INSERT INTO kun_sentence (id, kanji_id, kun_jp_text, kun_kr_text)
VALUES 
(6, 6, '人は誰でも間違える。', '사람은 누구나 실수한다.');

---------------------------------

-- 음독 문장
INSERT INTO on_sentence (kanji_id, on_jp_text, on_kr_text)
VALUES
  ((SELECT id FROM kanji WHERE glyph='学'), '彼は大学で日本文学を研究している。', '그는 대학에서 일본 문학을 연구하고 있다.'),
  ((SELECT id FROM kanji WHERE glyph='火'), '火山が噴火した。', '화산이 분화했다.'),
  ((SELECT id FROM kanji WHERE glyph='水'), '水曜日に会議があります。', '수요일에 회의가 있습니다.'),
  ((SELECT id FROM kanji WHERE glyph='木'), '木曜日は友達と映画を見る。', '목요일에는 친구와 영화를 본다.'),
  ((SELECT id FROM kanji WHERE glyph='山'), '富士山は日本で一番高い山です。', '후지산은 일본에서 가장 높은 산입니다.');

INSERT INTO on_sentence (id, kanji_id, on_jp_text, on_kr_text)
VALUES 
(6, 6, '東京の人口は多いです。', '도쿄의 인구는 많습니다.');
---------------------------------

SELECT * FROM JLPT_VOCAB;

-- 단어
INSERT INTO jlpt_vocab (word, reading, meaning_kr, jlpt_level, part_of_speech, example_jp, example_kr)
VALUES
-- N5
('勉強', 'べんきょう', '공부', 'N5', '명사・동사', '私は日本語を勉強します。', '저는 일본어를 공부합니다.'),
('食べる', 'たべる', '먹다', 'N5', '동사', 'ご飯を食べます。', '밥을 먹습니다.'),
('水', 'みず', '물', 'N5', '명사', '冷たい水を飲みたいです。', '차가운 물을 마시고 싶어요.'),
('猫', 'ねこ', '고양이', 'N5', '명사', 'この猫はとてもかわいいです。', '이 고양이는 정말 귀여워요.'),
('行く', 'いく', '가다', 'N5', '동사', '学校へ行きます。', '학교에 갑니다.'),
-- N4
('便利', 'べんり', '편리하다', 'N4', '형용동사', 'この店はとても便利です。', '이 가게는 매우 편리합니다.'),
('始める', 'はじめる', '시작하다', 'N4', '동사', '新しい仕事を始めました。', '새로운 일을 시작했습니다.'),
('広い', 'ひろい', '넓다', 'N4', '형용사', 'この部屋はとても広いです。', '이 방은 매우 넓어요.'),
('急ぐ', 'いそぐ', '서두르다', 'N4', '동사', '遅れないように急ぎましょう。', '늦지 않도록 서두릅시다.'),
('終わる', 'おわる', '끝나다', 'N4', '동사', '授業が終わりました。', '수업이 끝났습니다.');