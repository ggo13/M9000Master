system echo "\nAbout to update authentication plugin..."
ALTER USER  'dfr'@'%' IDENTIFIED WITH caching_sha2_password BY 'usi';
ALTER USER  'dfr'@'localhost' IDENTIFIED WITH caching_sha2_password BY 'usi';
FLUSH PRIVILEGES;
system echo "Done\n"