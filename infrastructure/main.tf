locals {
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
}

module "div-cos" {
  source                      = "git@github.com:hmcts/moj-module-webapp.git"
  product                     = "${var.product}-${var.component}"
  location                    = "${var.location}"
  env                         = "${var.env}"
  ilbIp                       = "${var.ilbIp}"
  is_frontend                 = false
  subscription                = "${var.subscription}"
  capacity                    = "${var.capacity}"
  common_tags                 = "${var.common_tags}"

  app_settings = {
    // logging vars
    REFORM_TEAM                                     = "${var.product}"
    REFORM_SERVICE_NAME                             = "${var.component}"
    REFORM_ENVIRONMENT                              = "${var.env}"
    CASE_FORMATTER_SERVICE_API_BASEURL              = "${local.case_formatter_baseurl}"
    CASE_VALIDATION_SERVICE_API_BASEURL             = "${local.validation_service_baseurl}"
    DOCUMENT_GENERATOR_SERVICE_API_BASEURL          = "${local.document_generator_baseurl}"
    CASE_MAINTENANCE_SERVICE_API_BASEURL            = "${local.case_maintenance_service_baseurl}"
    IDAM_API_URL                                    = "${var.idam_api_baseurl}"
    IDAM_API_REDIRECT_URL                           = "${local.petitioner_fe_baseurl}/authenticated"
    AUTH2_CLIENT_SECRET                             = "${data.azurerm_key_vault_secret.auth-idam-client-secret.value}"
    IDAM_CITIZEN_USERNAME                           = "${data.azurerm_key_vault_secret.auth-idam-citizen-username.value}"
    IDAM_CITIZEN_PASSWORD                           = "${data.azurerm_key_vault_secret.auth-idam-client-secret.value}"
  }
}

data "azurerm_key_vault" "div_key_vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.vaultName}"
}

data "azurerm_key_vault_secret" "ccd-submission-s2s-auth-secret" {
  name      = "ccd-submission-s2s-auth-secret"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "div-doc-s2s-auth-secret" {
  name      = "div-doc-s2s-auth-secret"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "auth-idam-client-secret" {
  name      = "idam-secret"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "auth-idam-citizen-username" {
  name      = "idam-citizen-username"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "auth-idam-citizen-password" {
  name      = "idam-citizen-password"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}