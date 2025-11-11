delimiter $$
USE m9000 $$
system echo "About to drop triggers and tables thats only required for Remote architecture"
# Trigger on dat_staging to update the dat_updates table to know about the recently created faults
DROP TRIGGER IF EXISTS dat_changes$$
# DROP table if exists dat_updates$$

# Trigger on ser to update the ser_updates table to know about the recently created ser
DROP TRIGGER IF EXISTS new_ser$$
# DROP table if exists ser_updates$$

DROP TRIGGER IF EXISTS new_alarms_log$$
# DROP table if exists alarms_log_updates$$

# In case of typo remove existing tables with typo
DROP TRIGGER IF EXISTS new_contiuous_comtrade_data$$
# DROP table if exists contiuous_comtrade_data_updates$$

DROP TRIGGER IF EXISTS new_continuous_comtrade_data$$
# DROP table if exists continuous_comtrade_data_updates$$


DROP TRIGGER IF EXISTS comtrade_details_changes$$
# DROP table if exists comtrade_details_updates$$

DROP TRIGGER IF EXISTS new_reports$$
# DROP table if exists reports_updates$$

DROP TRIGGER IF EXISTS ltr_dat_changes$$
# DROP table if exists ltr_updates$$

# Remove all the federated tables 
DROP table if exists federated_alarms_log$$
DROP table if exists federated_comtrade_details$$
DROP table if exists federated_continuous_comtrade_data$$
DROP table if exists federated_dat_staging$$
DROP table if exists federated_long_term_dat$$
DROP table if exists federated_reports$$
DROP table if exists federated_ser$$

delimiter ;
system echo "Done\n"
