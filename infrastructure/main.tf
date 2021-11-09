provider "azurerm" {
  features {}
}

locals {
  app_full_name = "${var.product}-${var.component}"
  local_env     = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "aat" : "saat" : var.env

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

module "db" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = var.product
  component          = var.component
  name               = "${local.app_full_name}-postgres-db"
  location           = var.location
  env                = var.env
  subscription       = var.subscription
  postgresql_user    = var.postgresql_user
  database_name      = var.database_name
  sku_name           = var.sku_name
  sku_capacity       = var.sku_capacity
  sku_tier           = "GeneralPurpose"
  storage_mb         = var.database_storage_mb
  common_tags        = var.common_tags
  postgresql_version = "9.6"
}

module "azure-media-services" {
  source      = "git@github.com:hmcts/cnp-module-azure-media-services"
  location    = var.location
  env         = var.env
  common_tags = var.common_tags
  product     = var.product
  enabled     = var.enable_azure_media_service
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

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name         = "${var.component}-POSTGRES-USER"
  value        = module.db.user_name
  key_vault_id = data.azurerm_key_vault.ccd_shared_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name         = "${var.component}-POSTGRES-PASS"
  value        = module.db.postgresql_password
  key_vault_id = data.azurerm_key_vault.ccd_shared_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS-DM" {
  name         = "${var.component}-POSTGRES-PASS"
  value        = module.db.postgresql_password
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name         = "${var.component}-POSTGRES-HOST"
  value        = module.db.host_name
  key_vault_id = data.azurerm_key_vault.ccd_shared_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name         = "${var.component}-POSTGRES-PORT"
  value        = module.db.postgresql_listen_port
  key_vault_id = data.azurerm_key_vault.ccd_shared_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = module.db.postgresql_database
  key_vault_id = data.azurerm_key_vault.ccd_shared_vault.id
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
  name         = "AppInsightsInstrumentationKey"
  key_vault_id = data.azurerm_key_vault.shared_key_vault.id
}

resource "azurerm_key_vault_secret" "local_app_insights_key" {
  name         = "RpaAppInsightsInstrumentationKey"
  value        = data.azurerm_key_vault_secret.app_insights_key.value
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

data "azurerm_subnet" "postgres" {
  name                 = "core-infra-subnet-0-${var.env}"
  resource_group_name  = "core-infra-${var.env}"
  virtual_network_name = "core-infra-vnet-${var.env}"
}

module "db-v11" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=postgresql_tf"
  product            = var.product
  component          = var.component
  name               = join("-", [var.product,var.component,"postgres-db-v11"])
  location           = var.location
  env                = var.env
  postgresql_user    = var.postgresql_user_v11
  database_name      = var.database_name_v11
  postgresql_version = "11"
  subnet_id          = data.azurerm_subnet.postgres.id
  sku_name           = "GP_Gen5_2"
  sku_tier           = "GeneralPurpose"
  common_tags        = var.common_tags
  subscription       = var.subscription
}

resource "azurerm_key_vault_secret" "POSTGRES-USER-V11" {
  name = "${var.component}-POSTGRES-USER-V11"
  value = module.db-v11.user_name
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS-V11" {
  name = "${var.component}-POSTGRES-PASS-V11"
  value = module.db-v11.postgresql_password
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-HOST-V11" {
  name = "${var.component}-POSTGRES-HOST-V11"
  value = module.db-v11.host_name
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PORT-V11" {
  name = "${var.component}-POSTGRES-PORT-V11"
  value = module.db-v11.postgresql_listen_port
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-DATABASE-V11" {
  name = "${var.component}-POSTGRES-DATABASE-V11"
  value = module.db-v11.postgresql_database
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}
