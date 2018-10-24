output "vaultName" {
  value = "${local.vaultName}"
}

output "vaultUri" {
  value = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

output "idam_s2s_url" {
  value = "${local.idam_s2s_url}"
}

output "draft_check_ccd_enabled" {
  value = "${var.draft_check_ccd_enabled}"
}


output "aos_responded_days_to_complete" {
  value = "${var.aos_responded_days_to_complete}"
}

output "aos_responded_awaiting_answer_days_to_respond" {
  value = "${var.aos_responded_awaiting_answer_days_to_respond}"
}