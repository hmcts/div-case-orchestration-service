locals {
  aseName                     = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  local_env                   = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"

  idam_s2s_url                = "http://rpe-service-auth-provider-${local.local_env}.service.core-compute-${local.local_env}.internal"
  case_formatter_baseurl      = "http://div-cfs-${local.local_env}.service.core-compute-${local.local_env}.internal"
  document_generator_baseurl  = "http://div-dgs-${local.local_env}.service.core-compute-${local.local_env}.internal"
  validation_service_baseurl  = "http://div-vs-${local.local_env}.service.core-compute-${local.local_env}.internal"

  previewVaultName          = "${var.product}-${var.component}"
  nonPreviewVaultName       = "${var.product}-${var.component}-${var.env}"
  vaultName                 = "${var.env == "preview" ? local.previewVaultName : local.nonPreviewVaultName}"

  nonPreviewVaultUri        = "${module.key-vault.key_vault_uri}"
  previewVaultUri           = "https://div-${var.component}-aat.vault.azure.net/"
  vaultUri                  = "${var.env == "preview"? local.previewVaultUri : local.nonPreviewVaultUri}"

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
  }
}

provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

module "key-vault" {
  source              = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  name                = "${local.vaultName}"
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.div-cos.resource_group_name}"
  # dcd_cc-dev group object ID
  product_group_object_id = "1c4f0704-a29e-403d-b719-b90c34ef14c9"
}

data "vault_generic_secret" "ccd-submission-s2s-auth-secret" {
  path = "secret/${var.vault_env}/ccidam/service-auth-provider/api/microservice-keys/divorceCcdSubmission"
}

data "vault_generic_secret" "div-doc-s2s-auth-secret" {
  path = "secret/${var.vault_env}/ccidam/service-auth-provider/api/microservice-keys/divorceDocumentGenerator"
}

resource "azurerm_key_vault_secret" "ccd-submission-s2s-auth-secret" {
  name      = "ccd-submission-s2s-auth-secret"
  value     = "${data.vault_generic_secret.ccd-submission-s2s-auth-secret.data["value"]}"
  vault_uri = "${local.vaultUri}"
}

resource "azurerm_key_vault_secret" "div-doc-s2s-auth-secret" {
  name      = "div-doc-s2s-auth-secret"
  value     = "${data.vault_generic_secret.div-doc-s2s-auth-secret.data["value"]}"
  vault_uri = "${local.vaultUri}"
}