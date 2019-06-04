#!/usr/bin/env bash

set -e

if [ -z "DIV_SCHEDULER_DB_USER_NAME" ] || [ -z "DIV_SCHEDULER_DB_PASSWORD" ]; then
  echo "ERROR: Missing environment variable. Set value for both 'DIV_SCHEDULER_DB_USER_NAME' and 'DIV_SCHEDULER_DB_PASSWORD'."
  exit 1
fi
echo "Runnning the filesdocker"
echo $DIV_SCHEDULER_DB_PASSWORD
echo $DIV_SCHEDULER_DB_USER_NAME

# Create role and database
psql -v ON_ERROR_STOP=1 --username postgres --set USERNAME=$DIV_SCHEDULER_DB_USER_NAME --set PASSWORD=$DIV_SCHEDULER_DB_PASSWORD <<-EOSQL
  CREATE USER :USERNAME WITH PASSWORD ':PASSWORD';

  CREATE DATABASE div_scheduler
    WITH OWNER = :USERNAME
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
EOSQL
