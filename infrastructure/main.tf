provider "azurerm" {
  version = "1.19.0"
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
  
  uk_gov_notify_email_templates     = "{APPLIC_SUBMISSION: 'c323844c-5fb9-4ba4-8290-b84139eb033c', APPLICANT_CO_RESPONDENT_RESPONDS_AOS_NOT_SUBMITTED: 'e07cbeb8-c2e0-4ba5-84ba-b9bd1ab04b0a', APPLICANT_CO_RESPONDENT_RESPONDS_AOS_SUBMITTED_NO_DEFEND: '369169ef-c6cb-428c-abbd-427aaa50c2a3', AOS_RECEIVED_NO_ADMIT_ADULTERY: '015fb73a-3be2-49d8-8ed8-a4078025dae3', AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED: 'bc6ee2ec-f62b-4321-b19f-65e868f849eb', AOS_RECEIVED_NO_CONSENT_2_YEARS: '845d0114-0f74-43a4-b11c-8ebeceb01c5b', CO_RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION: '19a8844e-8112-4578-aa4c-dea6c054ab35', CO_RESPONDENT_UNDEFENDED_AOS_SUBMISSION_NOTIFICATION: '486c86ff-a0e2-4eb1-a84c-687641d746de', DN_SUBMISSION: 'edf3bce9-f63a-4be0-93a9-d0c80dff7983', GENERIC_UPDATE: '6ee6ec29-5e88-4516-99cb-2edc30256575', SAVE_DRAFT:'14074c06-87f1-4678-9238-4d71e741eb57', RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION: 'eac41143-b296-4879-ba60-a0ea6f97c757', RESPONDENT_SUBMISSION_CONSENT: '594dc500-93ca-4f4b-931b-acbf9ee83d25', RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED: '44e2dd30-4303-4f4c-a394-ce0b54af81dd', RESPONDENT_UNDEFENDED_AOS_SUBMISSION_NOTIFICATION: '277fd3f3-2fdb-4c79-9354-1b3db8d44cca', PETITIONER_CERTIFICATE_OF_ENTITLEMENT_NOTIFICATION: '9937c8bc-dc7a-4210-a25b-20aceb82d48d', PETITIONER_CLARIFICATION_REQUEST_EMAIL_NOTIFICATION: '686ce418-6d76-48ce-b903-a87d2b832125'}"

  previewVaultName = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  asp_name = "${var.env == "prod" ? "div-cos-prod" : "${var.raw_product}-${var.env}"}"
  asp_rg = "${var.env == "prod" ? "div-cos-prod" : "${var.raw_product}-${var.env}"}"
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
    UK_GOV_NOTIFY_EMAIL_TEMPLATES                   = "${local.uk_gov_notify_email_templates}"
    UK_GOV_NOTIFY_EMAIL_TEMPLATE_VARS               = "${var.uk_gov_notify_email_template_vars}"
    FEATURE_TOGGLE_520                              = "${var.feature_toggle_520}"
    AOS_RESPONDED_DAYS_TO_COMPLETE                  = "${var.aos_responded_days_to_complete}"
    AOS_RESPONDED_AWAITING_ANSWER_DAYS_TO_RESPOND   = "${var.aos_responded_awaiting_answer_days_to_respond}"

    FEATURE_TOGGLE_SERVICE_API_BASEURL             = "${local.feature_toggle_baseurl}"
    SEND_LETTER_SERIVCE_BASEURL                    = "${local.send_letter_service_baseurl}"
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
