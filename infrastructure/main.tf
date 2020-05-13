provider "azurerm" {
  version = "1.44.0"
}

locals {
  vaultName = "${var.product}-${var.env}"
}

module "div-scheduler-db" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = "${var.product}-${var.component}"
  location           = "${var.location_db}"
  env                = "${var.env}"
  database_name      = "div_scheduler"
  postgresql_user    = "div_scheduler"
  postgresql_version = "10"
  sku_name           = "GP_Gen5_2"
  sku_tier           = "GeneralPurpose"
  common_tags        = "${var.common_tags}"
  subscription       = "${var.subscription}"
}

resource "azurerm_key_vault_secret" "postgresql-user" {
  name      = "${var.component}-postgresql-user"
  value     = "${module.div-scheduler-db.user_name}"
  key_vault_id = "${data.azurerm_key_vault.div_key_vault.id}"
}

resource "azurerm_key_vault_secret" "postgresql-password" {
  name      = "${var.component}-postgresql-password"
  value     = "${module.div-scheduler-db.postgresql_password}"
  key_vault_id = "${data.azurerm_key_vault.div_key_vault.id}"
}

data "azurerm_key_vault" "div_key_vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.vaultName}"
}

data "azurerm_key_vault_secret" "ccd-submission-s2s-auth-secret" {
  name      = "ccd-submission-s2s-auth-secret"
  key_vault_id = "${data.azurerm_key_vault.div_key_vault.id}"
}

data "azurerm_key_vault_secret" "div-doc-s2s-auth-secret" {
  name      = "div-doc-s2s-auth-secret"
  key_vault_id = "${data.azurerm_key_vault.div_key_vault.id}"
}

data "azurerm_key_vault_secret" "frontend_secret" {
    name      = "frontend-secret"
    key_vault_id = "${data.azurerm_key_vault.div_key_vault.id}"
}

data "azurerm_key_vault_secret" "auth-idam-client-secret" {
  name      = "idam-secret"
  key_vault_id = "${data.azurerm_key_vault.div_key_vault.id}"
}

data "azurerm_key_vault_secret" "auth-idam-citizen-username" {
  name      = "idam-citizen-username"
  key_vault_id = "${data.azurerm_key_vault.div_key_vault.id}"
}

data "azurerm_key_vault_secret" "auth-idam-citizen-password" {
  name      = "idam-citizen-password"
  key_vault_id = "${data.azurerm_key_vault.div_key_vault.id}"
}

data "azurerm_key_vault_secret" "auth-idam-caseworker-username" {
  name      = "idam-caseworker-username"
  key_vault_id = "${data.azurerm_key_vault.div_key_vault.id}"
}

data "azurerm_key_vault_secret" "auth-idam-caseworker-password" {
  name      = "idam-caseworker-password"
  key_vault_id = "${data.azurerm_key_vault.div_key_vault.id}"
}

data "azurerm_key_vault_secret" "uk-gov-notify-api-key" {
  name      = "uk-gov-notify-api-key"
  key_vault_id = "${data.azurerm_key_vault.div_key_vault.id}"
}
