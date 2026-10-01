terraform {
  backend "azurerm" {}

  required_providers {
    azuread = {
      source  = "hashicorp/azuread"
      version = "3.10.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 5.7.0"
    }
  }
}
