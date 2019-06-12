vault_env = "preprod"
idam_api_baseurl = "https://idam-api.aat.platform.hmcts.net"
idam_strategic_enabled = "true"

capacity = "2"

instance_size = "I3"

health_check_ttl = 30000

scheduler_enabled = "true"

scheudler_schedules_create_bulk_cases_cron = "0 0/30 * ? * * *"