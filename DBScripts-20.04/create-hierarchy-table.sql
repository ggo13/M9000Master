DELIMITER $$
CREATE TABLE IF NOT EXISTS hierarchy (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  parent_id int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (parent_id) REFERENCES hierarchy (id) 
    ON DELETE CASCADE ON UPDATE CASCADE
)$$

DELIMITER ;
