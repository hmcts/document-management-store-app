terraform {
    backend "azurerm" {}
}

data "terraform_remote_state" "core_apps_infrastructure" {
    backend = "azurerm"

    config {
        resource_group_name  = "contino-moj-tf-state"
        storage_account_name = "continomojtfstate"
        container_name       = "contino-moj-tfstate-container"
        key                  = "core-infra-sample/${var.infrastructure_env}/terraform.tfstate"
    }
}

data "terraform_remote_state" "core_apps_compute" {
    backend = "azurerm"

    config {
        resource_group_name  = "contino-moj-tf-state"
        storage_account_name = "continomojtfstate"
        container_name       = "contino-moj-tfstate-container"
        key                  = "core-compute-sample/${var.infrastructure_env}/terraform.tfstate"
    }
}
