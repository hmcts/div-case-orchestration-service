locals {
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
}


module "div-case-orchestration-service" {
  source = "git@github.com:hmcts/moj-module-webapp.git"
  product = "${var.product}-${var.microservice}"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"
  is_frontend = false
  subscription = "${var.subscription}"

  app_settings = {
    // logging vars
    REFORM_TEAM = "${var.product}"
    REFORM_SERVICE_NAME = "${var.microservice}"
    REFORM_ENVIRONMENT = "${var.env}"
  }
}

