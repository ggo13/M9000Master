use m9000;
-- Execute ALTER TABLE statements directly
SET SESSION group_concat_max_len = 1000000;

SET @alter_statements = (
  SELECT GROUP_CONCAT('ALTER TABLE ', table_name, ' ENGINE=InnoDB;') 
  FROM information_schema.tables 
  WHERE table_schema = 'm9000' AND engine = 'MyISAM'
);

-- Execute the generated SQL
PREPARE alter_statements FROM @alter_statements;
EXECUTE alter_statements;
DEALLOCATE PREPARE alter_statements;
