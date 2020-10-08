terraform {
  backend "azurerm" {}

  required_providers {
          azurerm = {
            source  = "hashicorp/azurerm"
            version = "~> 2.30.0"
          }
  }
}
