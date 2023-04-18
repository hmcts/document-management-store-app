terraform {
  backend "azurerm" {}

  required_providers {
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.37.1"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.52.0"
    }
   }
}
