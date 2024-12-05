provider "azurerm" {
  features {
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
  }
}

provider "azurerm" {
  features {}
  skip_provider_registration = true
  alias                      = "cft_vnet"
  subscription_id            = var.aks_subscription_id
}

locals {
  app_full_name = "${var.product}-${var.component}"
  local_env     = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "aat" : "saat" : var.env
  db_name = "${var.product}-${var.component}-postgres-db-flex"

  // Shared Resource Group - CCD
  previewResourceGroup    = "${var.shared_product}-shared-aat"
  nonPreviewResourceGroup = "${var.shared_product}-shared-${var.env}"
  sharedResourceGroup     = (var.env == "preview" || var.env == "spreview") ? local.previewResourceGroup : local.nonPreviewResourceGroup

  // Storage Account - shared
  previewStorageAccountName    = "${var.shared_product}sharedaat"
  nonPreviewStorageAccountName = "${var.shared_product}shared${var.env}"
  storageAccountName           = (var.env == "preview" || var.env == "spreview") ? local.previewStorageAccountName : local.nonPreviewStorageAccountName

  // Storage Account - dm-store
  previewStorageAccountNameDM    = "${var.raw_product}storedocaat"
  nonPreviewStorageAccountNameDM = "${var.raw_product}storedoc${var.env}"
  storageAccountNameDM           = (var.env == "preview" || var.env == "spreview") ? local.previewStorageAccountNameDM : local.nonPreviewStorageAccountNameDM

  // Shared Vault - CCD
  previewVaultName    = "${var.shared_product}-aat"
  nonPreviewVaultName = "${var.shared_product}-${var.env}"
  vaultName           = (var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName
}

resource "azurerm_storage_container" "document_container" {
  name                  = "${local.app_full_name}-docstore-${var.env}"
  storage_account_name  = local.storageAccountNameDM
  container_access_type = "private"
}

data "azurerm_key_vault" "ccd_shared_vault" {
  name                = local.vaultName
  resource_group_name = local.sharedResourceGroup
}

data "azurerm_key_vault" "dm_shared_vault" {
  name                = "dm-${var.env}"
  resource_group_name = "dm-shared-${var.env}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS-DM" {
  name         = "${var.component}-POSTGRES-PASS"
  value        = module.db-v15.password
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

data "azurerm_key_vault_secret" "dm_store_storageaccount_primary_connection_string" {
  name         = "dm-store-storage-account-primary-connection-string"
  key_vault_id = data.azurerm_key_vault.ccd_shared_vault.id
}

resource "azurerm_key_vault_secret" "primary_connection_string" {
  name         = "dm-store-storage-account-primary-connection-string"
  value        = data.azurerm_key_vault_secret.dm_store_storageaccount_primary_connection_string.value
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

data "azurerm_key_vault_secret" "dm_store_storageaccount_secondary_connection_string" {
  name         = "dm-store-storage-account-secondary-connection-string"
  key_vault_id = data.azurerm_key_vault.ccd_shared_vault.id
}

resource "azurerm_key_vault_secret" "secondary_connection_string" {
  name         = "dm-store-storage-account-secondary-connection-string"
  value        = data.azurerm_key_vault_secret.dm_store_storageaccount_secondary_connection_string.value
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

data "azurerm_key_vault" "shared_key_vault" {
  name                = "rpa-${var.env}"
  resource_group_name = "rpa-${var.env}"
}

# Load AppInsights key from rpa vault
data "azurerm_key_vault_secret" "app_insights_key" {
  name         = "EmAppInsightsInstrumentationKey"
  key_vault_id = data.azurerm_key_vault.shared_key_vault.id
}

resource "azurerm_key_vault_secret" "local_app_insights_key" {
  name         = "RpaAppInsightsInstrumentationKey"
  value        = data.azurerm_key_vault_secret.app_insights_key.value
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

data "azurerm_key_vault_secret" "app_insights_connection_string" {
  name         = "em-app-insights-connection-string"
  key_vault_id = data.azurerm_key_vault.shared_key_vault.id
}

resource "azurerm_key_vault_secret" "local_app_insights_connection_string" {
  name         = "app-insights-connection-string"
  value        = data.azurerm_key_vault_secret.app_insights_connection_string.value
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

data "azurerm_subnet" "postgres" {
  name                 = "core-infra-subnet-0-${var.env}"
  resource_group_name  = "core-infra-${var.env}"
  virtual_network_name = "core-infra-vnet-${var.env}"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name         = "${var.component}-POSTGRES-USER"
  value        = module.db-v15.username
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-HOST" {
  name         = "${var.component}-POSTGRES-HOST"
  value        = module.db-v15.fqdn
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PORT" {
  name         = "${var.component}-POSTGRES-PORT"
  value        = "5432"
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-DATABASE" {
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = "evidence"
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

# FlexibleServer v15
module "db-v15" {
  providers = {
    azurerm.postgres_network = azurerm.cft_vnet
  }
  source               = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  env                  = var.env
  product              = var.product
  component            = var.component
  common_tags          = var.common_tags
  name                 = "${local.app_full_name}-postgres-db-v15"
  pgsql_version        = "15"
  admin_user_object_id = var.jenkins_AAD_objectId
  business_area        = "CFT"
  action_group_name           = join("-", [local.db_name, var.action_group_name])
  email_address_key           = var.email_address_key
  email_address_key_vault_id  = data.azurerm_key_vault.em_key_vault.id
  # The original subnet is full, this is required to use the new subnet for new databases
  subnet_suffix = "expanded"
  pgsql_databases = [
    {
      name : "evidence"
    }
  ]
  pgsql_server_configuration = [
    {
      name  = "azure.extensions"
      value = "plpgsql,pg_stat_statements,pg_buffercache,hypopg,uuid-ossp"
    }
  ]
  //Below attributes needs to be overridden for Perftest & Prod
  pgsql_sku                      = var.pgsql_sku
  pgsql_storage_mb               = var.pgsql_storage_mb
  force_user_permissions_trigger = "1"
}
