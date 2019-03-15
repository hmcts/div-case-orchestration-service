// COS Alerts Action Groups

data "azurerm_key_vault_secret" "cos_failure_email_secret" {
  name      = "divorce-case-orchestration-failure-email"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

module "cos-failure-action-group" {
  source   = "git@github.com:hmcts/cnp-module-action-group"
  location = "global"
  env      = "${var.env}"

  resourcegroup_name     = "${local.vaultName}"
  action_group_name      = "Divorce Case Orchestration Failure Alert - ${var.env}"
  short_name             = "COS_alert"
  email_receiver_name    = "Divorce Case Orchestration Alerts"
  email_receiver_address = "${data.azurerm_key_vault_secret.cos_failure_email_secret.value}"
}

output "cos_failure_action_group_name" {
  value = "${module.cos-failure-action-group.action_group_name}"
}