variable "product" {
  default = "div"
}

variable "microservice" {
  default = "orch"
}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "ilbIp" {}

variable "subscription" {}

variable "jenkins_AAD_objectId" {
  type                        = "string"
  description                 = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

