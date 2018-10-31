variable "product" {
  default = "div"
}

variable "raw_product" {
   default = "div"
}

variable "component" {
  default = "cos"
}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "ilbIp" {}

variable "subscription" {}

variable "jenkins_AAD_objectId" {
  type          = "string"
  description   = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "appinsights_instrumentation_key" {
  description = "Instrumentation key of the App Insights instance this webapp should use. Module will create own App Insights resource if this is not provided"
  default = ""
}

variable "tenant_id" {}

variable "idam_api_baseurl" {
  type = "string"
}

variable "capacity" {
  default = "1"
}

variable "vault_env" {}

variable "common_tags" {
  type = "map"
}

variable "draft_check_ccd_enabled" {
    default = true
}

variable "uk_gov_notify_email_templates" {
    type = "string"
}

variable "uk_gov_notify_email_template_vars" {
    type = "string"
}

variable "service_auth_microservice_name" {
  default = "divorce_frontend"
}

variable "aos_responded_days_to_complete" {
  default = 7
}

variable "aos_responded_awaiting_answer_days_to_respond" {
  default = 21
}
