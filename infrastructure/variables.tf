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

variable "service_auth_microservice_name" {
  default = "divorce_frontend"
}