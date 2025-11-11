system echo "\nStarting to deploy db for local architecture\n"
source Create_Users.sql
source M9k-StationMaster-Create-Tables.sql
-- source M9k-StationMaster-Local-Util.sql
-- source m9k-create-server-link.sql
-- source create-federated-tables.sql
source drop-partitions.sql
source create-partitions.sql
source create-sp-to-remove-old-data.sql
source create-new-events.sql
source init_file.sql
source m9k-insert-user-details.sql
system echo "Done\n"