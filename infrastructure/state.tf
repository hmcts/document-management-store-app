terraform {
  backend "azurerm" {}

  required_providers {
    azuread = {
      source  = "hashicorp/azuread"
      version = "3.0.1"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.3.0"
    }
  }
}
