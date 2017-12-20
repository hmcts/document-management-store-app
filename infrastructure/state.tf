terraform {
  backend "azurerm" {}
}

data "terraform_remote_state" "core_apps_infrastructure" {
  backend = "azurerm"

  config {
    resource_group_name  = "mgmt-state-store-${var.ssenv}"
    storage_account_name = "mgmtstatestore${var.ssenv}"
    container_name       = "mgmtstatestorecontainer${var.ssenv}"
    key                  = "core-infra/${var.env}/terraform.tfstate"
  }
}

data "terraform_remote_state" "core_apps_compute" {
  backend = "azurerm"

  config {
    resource_group_name  = "mgmt-state-store-${var.ssenv}"
    storage_account_name = "mgmtstatestore${var.ssenv}"
    container_name       = "mgmtstatestorecontainer${var.ssenv}"
    key                  = "core-compute/${var.env}/terraform.tfstate"
  }
}
