provider "azurerm" {
  version = "1.22.1"
}

locals {
  aseName                     = "core-compute-${var.env}"
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
  feature_toggle_baseurl            = "http://rpe-feature-toggle-api-${local.local_env}.service.core-compute-${local.local_env}.internal"
  send_letter_service_baseurl       = "http://rpe-send-letter-service-${local.local_env}.service.core-compute-${local.local_env}.internal"


  previewVaultName = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  asp_name = "${var.env == "prod" ? "div-cos-prod" : "${var.raw_product}-${var.env}"}"
  asp_rg = "${var.env == "prod" ? "div-cos-prod" : "${var.raw_product}-${var.env}"}"
  db_connection_options = "?sslmode=require"
}

module "div-cos" {
  source                          = "git@github.com:hmcts/moj-module-webapp.git"
  product                         = "${var.product}-${var.component}"
  location                        = "${var.location}"
  env                             = "${var.env}"
  ilbIp                           = "${var.ilbIp}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  is_frontend                     = false
  subscription                    = "${var.subscription}"
  capacity                        = "${var.capacity}"
  common_tags                     = "${var.common_tags}"
  asp_name                        = "${local.asp_name}"
  asp_rg                          = "${local.asp_rg}"
  instance_size                   = "${var.instance_size}"

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
    IDAM_CASEWORKER_USERNAME                        = "${data.azurerm_key_vault_secret.auth-idam-caseworker-username.value}"
    IDAM_CASEWORKER_PASSWORD                        = "${data.azurerm_key_vault_secret.auth-idam-caseworker-password.value}"
    IDAM_STRATEGIC_ENABLED                          = "${var.idam_strategic_enabled}"
    UK_GOV_NOTIFY_API_KEY                           = "${data.azurerm_key_vault_secret.uk-gov-notify-api-key.value}"
    AOS_RESPONDED_DAYS_TO_COMPLETE                  = "${var.aos_responded_days_to_complete}"
    AOS_RESPONDED_AWAITING_ANSWER_DAYS_TO_RESPOND   = "${var.aos_responded_awaiting_answer_days_to_respond}"
    MANAGEMENT_ENDPOINT_HEALTH_CACHE_TIMETOLIVE     = "${var.health_check_ttl}"
    SCHEDULER_RE_CREATE                             = "${var.scheduler_re_create}"
    SCHEDULER_ENABLED                               = "${var.scheduler_enabled}"
    SCHEDULER_SCHEDULES_CREATE_BULK_CASES_CRON      = "${var.scheudler_schedules_create_bulk_cases_cron}"

    FEATURE_TOGGLE_SERVICE_API_BASEURL             = "${local.feature_toggle_baseurl}"
    SEND_LETTER_SERIVCE_BASEURL                    = "${local.send_letter_service_baseurl}"
    DIV_SCHEDULER_DB_HOST                          = "${module.div-scheduler-db.host_name}"
    DIV_SCHEDULER_DB_PORT                          = "${module.div-scheduler-db.postgresql_listen_port}"
    DIV_SCHEDULER_DB_USER_NAME                     = "${module.div-scheduler-db.user_name}"
    DIV_SCHEDULER_DB_PASSWORD                      = "${module.div-scheduler-db.postgresql_password}"
    DIV_SCHEDULER_DB_NAME                          = "${module.div-scheduler-db.postgresql_database}"
    FLYWAY_URL                                     = "jdbc:postgresql://${module.div-scheduler-db.host_name}:${module.div-scheduler-db.postgresql_listen_port}/${module.div-scheduler-db.postgresql_database}${local.db_connection_options}"
    FLYWAY_USER                                    = "${module.div-scheduler-db.user_name}"
    FLYWAY_PASSWORD                                = "${module.div-scheduler-db.postgresql_password}"
    FLYWAY_NOOP_STRATEGY                           = "true"

    FEATURE_RESP_SOLICITOR_DETAILS                 = "${var.feature_resp_solicitor_details}"
  }
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
