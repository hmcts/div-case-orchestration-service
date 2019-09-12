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

variable "subscription" {}

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
variable "aos_responded_days_to_complete" {
  default = 7
}

variable "aos_responded_awaiting_answer_days_to_respond" {
  default = 21
}

variable "location_db" {
  type    = "string"
  default = "UK South"
}
variable "scheudler_schedules_create_bulk_cases_cron" {
  type    = "string"
  default = "0 0 4 ? * * *"
}

