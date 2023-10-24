terraform {
  backend "azurerm" {}

  required_providers {
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.44.1"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.77.0"
    }
   }
}
