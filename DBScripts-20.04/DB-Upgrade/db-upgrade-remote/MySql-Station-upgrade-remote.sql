system echo "\nStarting to upgrade database\n"
source M9k-StationMaster-Remote-Util.sql
source recreate-federated-tables.sql
source create-hierarchy-table.sql
source create-new-events.sql
source m9k-alter-tables.sql
source alter-cont.sql
source m9k-create-config-tables.sql
source alter-contAnalog_Users.sql
system echo "Done\n"