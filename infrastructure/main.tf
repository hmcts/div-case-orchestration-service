locals {
  aseName                     = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  local_env                   = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"

  idam_s2s_url                      = "http://rpe-service-auth-provider-${local.local_env}.service.core-compute-${local.local_env}.internal"
  case_formatter_baseurl            = "http://div-cfs-${local.local_env}.service.core-compute-${local.local_env}.internal"
  document_generator_baseurl        = "http://div-dgs-${local.local_env}.service.core-compute-${local.local_env}.internal"
  validation_service_baseurl        = "http://div-vs-${local.local_env}.service.core-compute-${local.local_env}.internal"
  case_maintenance_service_baseurl  = "http://div-cms-${local.local_env}.service.core-compute-${local.local_env}.internal"
  fees_and_payments_service_baseurl = "http://div-fps-${local.local_env}.service.core-compute-${local.local_env}.internal"
  payment_api_baseurl               = "http://payment-api-${local.local_env}.service.core-compute-${local.local_env}.internal"
  service_auth_provider_baseurl     = "http://rpe-service-auth-provider-${local.local_env}.service.core-compute-${local.local_env}.internal"
  petitioner_fe_baseurl             = "https://div-pfe-${local.local_env}.service.core-compute-${local.local_env}.internal"

  previewVaultName = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"
  
  asp_name = "${var.env == "prod" ? "div-cos-prod" : "${var.product}-${var.env}"}"
  asp_rg = "${var.env == "prod" ? "div-cos-prod" : "${var.product}-${var.env}"}"
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
  asp_name                        = "${local.asp_name}"
  asp_rg                          = "${local.asp_rg}"

  app_settings = {
    // logging vars
    REFORM_TEAM                                     = "${var.product}"
    REFORM_SERVICE_NAME                             = "${var.component}"
    REFORM_ENVIRONMENT                              = "${var.env}"
    CASE_FORMATTER_SERVICE_API_BASEURL              = "${local.case_formatter_baseurl}"
    CASE_VALIDATION_SERVICE_API_BASEURL             = "${local.validation_service_baseurl}"
    DOCUMENT_GENERATOR_SERVICE_API_BASEURL          = "${local.document_generator_baseurl}"
    CASE_MAINTENANCE_SERVICE_API_BASEURL            = "${local.case_maintenance_service_baseurl}"
    FEES_AND_PAYMENTS_SERVICE_API_BASEURL           = "${local.fees_and_payments_service_baseurl}"
    PAYMENT_API_BASEURL                             = "${local.payment_api_baseurl}"
    SERVICE_AUTH_PROVIDER_URL                       = "${local.service_auth_provider_baseurl}"
    SERVICE_AUTH_MICROSERVICE                       = "${var.service_auth_microservice_name}"
    SERVICE_AUTH_SECRET                             = "${data.azurerm_key_vault_secret.frontend_secret.value}"
    IDAM_API_URL                                    = "${var.idam_api_baseurl}"
    IDAM_API_REDIRECT_URL                           = "${local.petitioner_fe_baseurl}/authenticated"
    AUTH2_CLIENT_SECRET                             = "${data.azurerm_key_vault_secret.auth-idam-client-secret.value}"
    IDAM_CITIZEN_USERNAME                           = "${data.azurerm_key_vault_secret.auth-idam-citizen-username.value}"
    IDAM_CITIZEN_PASSWORD                           = "${data.azurerm_key_vault_secret.auth-idam-citizen-password.value}"
    UK_GOV_NOTIFY_API_KEY                           = "${data.azurerm_key_vault_secret.uk-gov-notify-api-key.value}"
    UK_GOV_NOTIFY_EMAIL_TEMPLATES                   = "${var.uk_gov_notify_email_templates}"
    UK_GOV_NOTIFY_EMAIL_TEMPLATE_VARS               = "${var.uk_gov_notify_email_template_vars}"
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

data "azurerm_key_vault_secret" "uk-gov-notify-api-key" {
  name      = "uk-gov-notify-api-key"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}
