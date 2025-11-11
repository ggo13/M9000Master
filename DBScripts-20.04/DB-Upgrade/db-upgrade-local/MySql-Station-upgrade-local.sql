system echo "\nStarting to upgrade database\n"
source M9k-StationMaster-Local-Util.sql
source create-partitions.sql
source drop-partitions.sql
source create-sp-to-remove-old-data.sql
-- source recreate-federated-tables.sql
source create-hierarchy-table.sql
source create-new-events.sql

source m9k-create-config-tables.sql
source m9k-alter-tables.sql
source alter-cont.sql
source alter-contAnalog_Users.sql
system echo "Done\n"