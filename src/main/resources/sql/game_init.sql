CREATE TABLE player_game_info
(
  id varchar(255) NOT NULL,
  game_id int NOT NULL, -- 游戏ID
  score int,  -- 最高分数
  num int,     -- 游戏次数
  primary key (id, game_id)
);