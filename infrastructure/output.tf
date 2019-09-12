output "vaultName" {
  value = "${local.vaultName}"
}

output "vaultUri" {
  value = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

output "test_environment" {
  value = "${local.local_env}"
}
output "service_auth_provider_url" {
  value = "${local.idam_s2s_url}"
}

output "case_maintenance_service_api_baseurl" {
  value = "${local.case_maintenance_service_baseurl}"
}
