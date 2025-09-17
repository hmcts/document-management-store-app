terraform {
  backend "azurerm" {}

  required_providers {
    azuread = {
      source  = "hashicorp/azuread"
      version = "3.5.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.44.0"
    }
  }
}
