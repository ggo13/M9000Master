use m9000;
delimiter $$
system echo "About to create server link for remote configuration..."
drop server if exists fedlink $$
CREATE SERVER fedlink
FOREIGN DATA WRAPPER mysql
OPTIONS (USER 'dfr', PASSWORD 'usi', HOST 'usi-pc', PORT 3306, DATABASE 'm9000')$$
system echo "Done\n"
delimiter ;