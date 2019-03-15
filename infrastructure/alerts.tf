module "cos-alert" {
  source = "git@github.com:hmcts/cnp-module-metric-alert"
  location = "${var.location}"

  app_insights_name = "div-${var.env}"

  alert_name = "COS Alert"
  alert_desc = "Triggers when too many requests failedl."
  app_insights_query = "requests | where resultCode == \"400\" or resultCode startswith \"5\" and duration > 10000 | where name !contains \"health\""
  custom_email_subject = "Warning: COS Alert"
  frequency_in_minutes = 5
  time_window_in_minutes = 5
  severity_level = "2"
  action_group_name = "COS alert recipients"
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold = 5
  resourcegroup_name = "${local.vaultName}"
}