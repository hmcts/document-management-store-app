terraform {
  backend "azurerm" {}

  required_providers {
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.49.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.102.0"
    }
   }
}
