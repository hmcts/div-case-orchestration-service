provider "azurerm" {
  features {}
}

locals {
  vaultName = "${var.product}-${var.env}"
  sendgrid_env = {
    prod = "prod"
    aat  = "nonprod"
  }
}

module "div-scheduler-db" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = "${var.product}-${var.component}"
  location           = var.location_db
  env                = var.env
  database_name      = "div_scheduler"
  postgresql_user    = "div_scheduler"
  postgresql_version = "10"
  sku_name           = "GP_Gen5_2"
  sku_tier           = "GeneralPurpose"
  common_tags        = var.common_tags
  subscription       = var.subscription
}

data "azurerm_key_vault" "div_key_vault" {
  name                = local.vaultName
  resource_group_name = local.vaultName
}

resource "azurerm_key_vault_secret" "postgresql-user" {
  name         = "${var.component}-postgresql-user"
  value        = module.div-scheduler-db.user_name
  key_vault_id = data.azurerm_key_vault.div_key_vault.id
}

resource "azurerm_key_vault_secret" "postgresql-password" {
  name         = "${var.component}-postgresql-password"
  value        = module.div-scheduler-db.postgresql_password
  key_vault_id = data.azurerm_key_vault.div_key_vault.id
}

data "azurerm_key_vault" "sendgrid" {
  name                = "sendgrid${local.sendgrid_env[var.env]}"
  resource_group_name = "sendgrid${local.sendgrid_env[var.env]}"
}

data "azurerm_key_vault_secret" "sendgrid-api-key" {
  name         = "hmcts-divorce-api-key"
  key_vault_id = data.azurerm_key_vault.sendgrid.id
}

resource "azurerm_key_vault_secret" "spring-mail-username" {
  name         = "spring-mail-username"
  value        = "apikey"
  key_vault_id = data.azurerm_key_vault.id
}

resource "azurerm_key_vault_secret" "spring-mail-password" {
  name         = "spring-mail-password"
  value        = data.azurerm_key_vault_secret.sendgrid-api-key.value
  key_vault_id = data.azurerm_key_vault.id
}