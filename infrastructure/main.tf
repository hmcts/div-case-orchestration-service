provider "azurerm" {
  version = "1.22.1"
}

locals {
  aseName   = "core-compute-${var.env}"
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"

  idam_s2s_url                      = "http://rpe-service-auth-provider-${local.local_env}.service.core-compute-${local.local_env}.internal"
  case_formatter_baseurl            = "http://div-cfs-${local.local_env}.service.core-compute-${local.local_env}.internal"
  document_generator_baseurl        = "http://div-dgs-${local.local_env}.service.core-compute-${local.local_env}.internal"
  validation_service_baseurl        = "http://div-vs-${local.local_env}.service.core-compute-${local.local_env}.internal"
  case_maintenance_service_baseurl  = "http://div-cms-${local.local_env}.service.core-compute-${local.local_env}.internal"
  fees_and_payments_service_baseurl = "http://div-fps-${local.local_env}.service.core-compute-${local.local_env}.internal"
  payment_api_baseurl               = "http://payment-api-${local.local_env}.service.core-compute-${local.local_env}.internal"
  service_auth_provider_baseurl     = "http://rpe-service-auth-provider-${local.local_env}.service.core-compute-${local.local_env}.internal"
  petitioner_fe_baseurl             = "https://div-pfe-${local.local_env}.service.core-compute-${local.local_env}.internal"
  feature_toggle_baseurl            = "http://rpe-feature-toggle-api-${local.local_env}.service.core-compute-${local.local_env}.internal"
  send_letter_service_baseurl       = "http://rpe-send-letter-service-${local.local_env}.service.core-compute-${local.local_env}.internal"

  previewVaultName    = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName           = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  asp_name              = "${var.env == "prod" ? "div-cos-prod" : "${var.raw_product}-${var.env}"}"
  asp_rg                = "${var.env == "prod" ? "div-cos-prod" : "${var.raw_product}-${var.env}"}"
  db_connection_options = "?sslmode=require"
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
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

resource "azurerm_key_vault_secret" "postgresql-password" {
  name      = "${var.component}-postgresql-password"
  value     = "${module.div-scheduler-db.postgresql_password}"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault" "div_key_vault" {
  name                = "${local.vaultName}"
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

data "azurerm_key_vault_secret" "frontend_secret" {
  name      = "frontend-secret"
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

data "azurerm_key_vault_secret" "auth-idam-caseworker-username" {
  name      = "idam-caseworker-username"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "auth-idam-caseworker-password" {
  name      = "idam-caseworker-password"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "uk-gov-notify-api-key" {
  name      = "uk-gov-notify-api-key"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}
