provider "azurerm" {
  features {}
}

provider "azurerm" {
  subscription_id            = local.cft_vnet[var.env].subscription
  skip_provider_registration = "true"
  features {}
  alias = "cft_vnet"
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
  cft_vnet = {
    sbox = {
      subscription = "b72ab7b7-723f-4b18-b6f6-03b0f2c6a1bb"
    }
    perftest = {
      subscription = "8a07fdcd-6abd-48b3-ad88-ff737a4b9e3c"
    }
    aat = {
      subscription = "96c274ce-846d-4e48-89a7-d528432298a7"
    }
    ithc = {
      subscription = "62864d44-5da9-4ae9-89e7-0cf33942fa09"
    }
    preview = {
      subscription = "8b6ea922-0862-443e-af15-6056e1c9b9a4"
    }
    prod = {
      subscription = "8cbc6f36-7c56-4963-9d36-739db5d00b27"
    }
    demo = {
      subscription = "d025fece-ce99-4df2-b7a9-b649d3ff2060"
    }
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

module "postgresql-14" {

  providers = {
    azurerm.postgres_network = azurerm.cft_vnet
  }

  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  env    = var.env
  name          = "div-${var.product}-v14-flexible"
  product       = var.product
  component     = var.component
  business_area = "cft"

  pgsql_databases = [
    {
      name : "div_scheduler_14"
    }
  ]

  pgsql_version = "14"
  admin_user_object_id = var.jenkins_AAD_objectId
  common_tags = var.common_tags
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
