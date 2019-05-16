#!/usr/bin/env bash

set -e

if [ -z "$DIVORCE_DB_USERNAME" ] || [ -z "$DIVORCE_DB_PASSWORD" ]; then
  echo "ERROR: Missing environment variable. Set value for both 'DIVORCE_DB_USERNAME' and 'DIVORCE_DB_PASSWORD'."
  exit 1
fi

# Create role and database
psql -v ON_ERROR_STOP=1 --username postgres --set USERNAME=$DIVORCE_DB_USERNAME --set PASSWORD=$DIVORCE_DB_PASSWORD <<-EOSQL
  CREATE USER :USERNAME WITH PASSWORD ':PASSWORD';

  CREATE DATABASE divorce
    WITH OWNER = :USERNAME
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
EOSQL
