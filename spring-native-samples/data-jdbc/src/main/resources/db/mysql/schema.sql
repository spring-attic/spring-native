CREATE TABLE IF NOT EXISTS category (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100),
  description VARCHAR(2000),
  age_group VARCHAR(20),
  created DATETIME,
  inserted BIGINT) engine=InnoDB;

CREATE TABLE IF NOT EXISTS Lego_Set (
  id INT(4),
  name VARCHAR(100),
  min_Age INT(4),
  max_Age INT(4)) engine=InnoDB;

CREATE TABLE IF NOT EXISTS Handbuch (
  handbuch_id INT(4),
  author VARCHAR(100),
  text LONGTEXT) engine=InnoDB;

CREATE TABLE IF NOT EXISTS Model (
  name VARCHAR(100),
  description LONGTEXT,
  lego_set INT(4)) engine=InnoDB;
