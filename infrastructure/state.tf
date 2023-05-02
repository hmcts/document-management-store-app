terraform {
  backend "azurerm" {}

  required_providers {
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.38.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.54.0"
    }
   }
}
