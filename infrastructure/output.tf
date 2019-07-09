output "vaultName" {
  value = "${local.vaultName}"
}

output "vaultUri" {
  value = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

output "test_environment" {
  value = "${local.local_env}"
}

output "idam_api_baseurl" {
  value = "${var.idam_api_baseurl}"
}

output "service_auth_provider_url" {
  value = "${local.idam_s2s_url}"
}

output "case_maintenance_service_api_baseurl" {
  value = "${local.case_maintenance_service_baseurl}"
}

output "aos_responded_days_to_complete" {
  value = "${var.aos_responded_days_to_complete}"
}

output "aos_responded_awaiting_answer_days_to_respond" {
  value = "${var.aos_responded_awaiting_answer_days_to_respond}"
}

output "scheudler_schedules_create_bulk_cases_cron" {
  value = "${var.scheudler_schedules_create_bulk_cases_cron}"
}

output "feature_resp_solicitor_details" {
  value = "${var.feature_resp_solicitor_details}"
}

output "documentation_swagger_enabled" {
  value = "${var.documentation_swagger_enabled}"
}