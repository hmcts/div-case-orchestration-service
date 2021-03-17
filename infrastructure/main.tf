provider "azurerm" {
  features {}
}

provider "azurerm" {
  alias = "sendgrid"
  features {}
  subscription_id = var.env != "prod" ? local.sendgrid_subscription.nonprod : local.sendgrid_subscription.prod
}

locals {
  vaultName = "${var.product}-${var.env}"
  sendgrid_subscription = {
    prod = "8999dec3-0104-4a27-94ee-6588559729d1"
    nonprod = "1c4f0704-a29e-403d-b719-b90c34ef14c9"
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
  provider = azurerm.sendgrid

  name                = var.env != "prod" ? "sendgridnonprod" : "sendgridprod"
  resource_group_name = var.env != "prod" ? "SendGrid-nonprod" : "SendGrid-prod"
}

data "azurerm_key_vault_secret" "sendgrid-api-key" {
  provider = azurerm.sendgrid
  
  name         = "hmcts-divorce-api-key"
  key_vault_id = data.azurerm_key_vault.sendgrid.id
}

resource "azurerm_key_vault_secret" "spring-mail-password" {
  name         = "spring-mail-password"
  value        = data.azurerm_key_vault_secret.sendgrid-api-key.value
  key_vault_id = data.azurerm_key_vault.div_key_vault.id
}
