DELIMITER $$
drop procedure if exists m9k_remove_old_data$$
create procedure m9k_remove_old_data(db_name TEXT, table_name TEXT, years_past INT)
begin
 SET @db_name = db_name;
 SET @table_name = table_name;
 SET @years_past = years_past;
 set @rows = 1;
 set @rows_deleted = 10000;
 if table_name = "ser" then set @search_col = "ser_date";
 else set @search_col = "updated";
 end if;
 
 while (@rows > 0)
 do
 	SET @sql_text = concat('delete from ',@db_name,'.',@table_name,' where ',@search_col, '< curdate() - interval ',@years_past,' year order by `id` limit 10000');
  	PREPARE stmt FROM @sql_text;
  	EXECUTE stmt;
  	DEALLOCATE PREPARE stmt;
    -- delete from db_name.table_name where search_col < curdate() - interval years_past year order by `id` limit 10000;
    set @rows = row_count();
    set @rows_deleted = @rows_deleted + row_count();
 end while;
end $$


DELIMITER ;