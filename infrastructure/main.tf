provider "azurerm" {
  features {}
}

locals {
  app_full_name = "${var.product}-${var.component}"
  ase_name      = "core-compute-${var.env}"
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

  sharedAppServicePlan       = "${var.shared_product}-${var.env}"
  sharedASPResourceGroup     = "${var.shared_product}-shared-${var.env}"

}

module "app" {
  source                       = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product                      = local.app_full_name
  location                     = var.location
  env                          = var.env
  ilbIp                        = var.ilbIp
  subscription                 = var.subscription
  capacity                     = var.capacity
  is_frontend                  = true #It's not front end but we need it so we can have a custom URL at the moment.
  https_only                   = "false"
  common_tags                  = var.common_tags
  asp_name                     = (var.asp_name == "use_shared") ? local.sharedAppServicePlan : var.asp_name
  asp_rg                       = (var.asp_rg == "use_shared") ? local.sharedASPResourceGroup : var.asp_rg
  website_local_cache_sizeinmb = 1600
  enable_ase                   = false

  app_settings = {
    POSTGRES_HOST     = module.db.host_name
    POSTGRES_PORT     = module.db.postgresql_listen_port
    POSTGRES_DATABASE = module.db.postgresql_database
    POSTGRES_USER     = module.db.user_name
    POSTGRES_PASSWORD = module.db.postgresql_password
    FORCE_APPLY       = "true"

    # JAVA_OPTS = "${var.java_opts}"
    # SERVER_PORT = "8080"

    # db
    SPRING_DATASOURCE_URL      = "jdbc:postgresql://${module.db.host_name}:${module.db.postgresql_listen_port}/${module.db.postgresql_database}?sslmode=require"
    SPRING_DATASOURCE_USERNAME = module.db.user_name
    SPRING_DATASOURCE_PASSWORD = module.db.postgresql_password

    MAX_FILE_SIZE = "${var.max_file_size_in_mb}MB"

    # idam
    IDAM_USER_BASE_URI = var.idam_api_url
    IDAM_S2S_BASE_URI  = "http://${var.s2s_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"

    # logging vars & healthcheck
    REFORM_SERVICE_NAME = local.app_full_name
    REFORM_TEAM         = var.team_name
    REFORM_SERVICE_TYPE = var.app_language
    REFORM_ENVIRONMENT  = var.env

    PACKAGES_NAME        = local.app_full_name
    PACKAGES_PROJECT     = var.team_name
    PACKAGES_ENVIRONMENT = var.env

    JSON_CONSOLE_PRETTY_PRINT = var.json_console_pretty_print
    LOG_OUTPUT                = var.log_output

    # addtional log
    ROOT_LOGGING_LEVEL   = var.root_logging_level
    LOG_LEVEL_SPRING_WEB = var.log_level_spring_web
    LOG_LEVEL_DM         = var.log_level_dm
    SHOW_SQL             = var.show_sql


    ENABLE_DB_MIGRATE = "false"

    # Toggles
    ENABLE_IDAM_HEALTH_CHECK            = var.enable_idam_healthcheck
    ENABLE_METADATA_SEARCH              = var.enable_metadata_search
    ENABLE_DOCUMENT_AND_METADATA_UPLOAD = var.enable_document_and_metadata_upload
    ENABLE_FOLDER_API                   = var.enable_folder_api
    ENABLE_DELETE                       = var.enable_delete
    ENABLE_TTL                          = var.enable_ttl
    ENABLE_THUMBNAIL                    = var.enable_thumbnail
    ENABLE_TESTING                      = var.enable_testing

    # Document Storage
    STORAGEACCOUNT_PRIMARY_CONNECTION_STRING   = data.azurerm_key_vault_secret.dm_store_storageaccount_primary_connection_string.value
    STORAGEACCOUNT_SECONDARY_CONNECTION_STRING = data.azurerm_key_vault_secret.dm_store_storageaccount_secondary_connection_string.value
    STORAGE_CONTAINER_DOCUMENT_CONTAINER_NAME  = azurerm_storage_container.document_container.name

    TEMP_FORCE_REDEPLOY = "1"

    TASK_ENV = "documentTaskLock-${var.env}"
  }
}

module "db" {
  source          = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product         = var.product
  component       = var.component
  name            = "${local.app_full_name}-postgres-db"
  location        = var.location
  env             = var.env
  subscription    = var.subscription
  postgresql_user = var.postgresql_user
  database_name   = var.database_name
  sku_name        = var.sku_name
  sku_capacity    = var.sku_capacity
  sku_tier        = "GeneralPurpose"
  storage_mb      = var.database_storage_mb
  common_tags     = var.common_tags
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
  name      = "AppInsightsInstrumentationKey"
  key_vault_id = data.azurerm_key_vault.shared_key_vault.id
}

resource "azurerm_key_vault_secret" "local_app_insights_key" {
  name         = "RpaAppInsightsInstrumentationKey"
  value        = data.azurerm_key_vault_secret.app_insights_key.value
  key_vault_id = data.azurerm_key_vault.dm_shared_vault.id
}
