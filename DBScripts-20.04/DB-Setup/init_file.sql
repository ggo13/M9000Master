use m9000;
system echo "About to drop and create partitions as per requirements..."
call CreatePartitions('m9000','cont',date(now()),5);
call CreatePartitions('m9000','contAnalog',date(now()),5);
-- call DropPartitions('m9000','cont',date(now()),30);
-- call DropPartitions('m9000','contAnalog',date(now()),5);
system echo "Done\n"