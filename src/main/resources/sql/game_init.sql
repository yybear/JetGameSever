CREATE TABLE player_game_info
(
  id varchar(255) PRIMARY KEY  NOT NULL,
  game_id int, -- 游戏ID
  score int,  -- 最高分数
  num int     -- 游戏次数
);