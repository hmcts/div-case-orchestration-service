#!/usr/bin/env bash

zap-api-scan.py -t ${TEST_URL//http/https}/v2/api-docs -f openapi -S -d -u ${SecurityRules} -P 1001 -l FAIL
 curl --fail http://0.0.0.0:1001/OTHER/core/other/jsonreport/?formMethod=GET --output report.json
 export LC_ALL=C.UTF-8
 export LANG=C.UTF-8

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
