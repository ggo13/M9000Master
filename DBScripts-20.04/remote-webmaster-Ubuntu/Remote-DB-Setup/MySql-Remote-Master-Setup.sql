system echo "\nStarting to deploy db for local architecture\n"
source Create_Users.sql
source M9k-Remote-Master-Create-Tables.sql
source m9k-insert-user-details.sql
system echo "Done\n"