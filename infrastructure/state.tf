terraform {
  backend "azurerm" {}

  required_providers {
          azurerm = {
            source  = "hashicorp/azurerm"
            version = "~> 2.3.0"
          }
  }
}
