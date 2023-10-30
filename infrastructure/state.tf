terraform {
  backend "azurerm" {}

  required_providers {
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.45.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.78.0"
    }
   }
}
