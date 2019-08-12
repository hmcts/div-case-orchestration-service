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

variable "instance_size" {
  default = "I2"
}

variable "vault_env" {}

variable "common_tags" {
  type = "map"
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

variable "idam_strategic_enabled" {
  default = "true"
}

variable "health_check_ttl" {
  type = "string"
  default = "4000"
}

variable "location_db" {
  type    = "string"
  default = "UK South"
}

variable "scheduler_re_create" {
  default = "true"
}

variable "scheduler_enabled" {
  default = "true"
}

variable "scheduler_make_cases_eligible_da_enabled" {
  default = "false"
}


variable "scheudler_schedules_create_bulk_cases_cron" {
  type    = "string"
  default = "0 0 4 ? * * *"
}

variable "scheduler_make_cases_eligible_da_cron" {
  type    = "string"
  default = "0 0 */3 ? * * *"
  description = "The scheduler job runs every 3 hours"
}

variable "awaiting_da_period" {
    type = "string"
    default = "43d"
    description = "The awaiting period for applicant to be eligible for Decree Absolute.Other time units are also supported, e.g. 'm'(i.e. minute),'s'(i.e. second)"
}

variable "feature_resp_solicitor_details" {
  type    = "string"
  default = "false"
}

variable "feature_dn_refusal" {
  type    = "string"
  default = "false"
}

variable "documentation_swagger_enabled" {
  default = "true"
}
