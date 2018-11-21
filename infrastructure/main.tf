locals {
  app_full_name = "${var.product}-${var.component}"
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"

  // Shared Resource Group - CCD
  previewResourceGroup = "${var.shared_product}-shared-aat"
  nonPreviewResourceGroup = "${var.shared_product}-shared-${var.env}"
  sharedResourceGroup = "${(var.env == "preview" || var.env == "spreview") ? local.previewResourceGroup : local.nonPreviewResourceGroup}"

  // Storage Account - shared
  previewStorageAccountName = "${var.shared_product}sharedaat"
  nonPreviewStorageAccountName = "${var.shared_product}shared${var.env}"
  storageAccountName = "${(var.env == "preview" || var.env == "spreview") ? local.previewStorageAccountName : local.nonPreviewStorageAccountName}"

  // Storage Account - dm-store
  previewStorageAccountNameDM = "${var.raw_product}storedocaat"
  nonPreviewStorageAccountNameDM = "${var.raw_product}storedoc${var.env}"
  storageAccountNameDM = "${(var.env == "preview" || var.env == "spreview") ? local.previewStorageAccountNameDM : local.nonPreviewStorageAccountNameDM}"

  // Shared Vault - CCD
  previewVaultName = "${var.shared_product}-aat"
  nonPreviewVaultName = "${var.shared_product}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  sharedAppServicePlan = "${var.shared_product}-${var.env}"
  sharedASPResourceGroup = "${var.shared_product}-shared-${var.env}"
}

module "app" {
  source = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product = "${local.app_full_name}"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"
  subscription = "${var.subscription}"
  capacity     = "${var.capacity}"
  is_frontend = true #It's not front end but we need it so we can have a custom URL at the moment.
  additional_host_name = "${local.app_full_name}-${var.env}.service.${var.env}.platform.hmcts.net"
  https_only="false"
  common_tags  = "${var.common_tags}"
  asp_name = "${(var.asp_name == "use_shared") ? local.sharedAppServicePlan : var.asp_name}"
  asp_rg = "${(var.asp_rg == "use_shared") ? local.sharedASPResourceGroup : var.asp_rg}"
  website_local_cache_sizeinmb = 0

  app_settings = {
    POSTGRES_HOST = "${module.db.host_name}"
    POSTGRES_PORT = "${module.db.postgresql_listen_port}"
    POSTGRES_DATABASE = "${module.db.postgresql_database}"
    POSTGRES_USER = "${module.db.user_name}"
    POSTGRES_PASSWORD = "${module.db.postgresql_password}"
    MAX_ACTIVE_DB_CONNECTIONS = 70

    # JAVA_OPTS = "${var.java_opts}"
    # SERVER_PORT = "8080"

    # db
    SPRING_DATASOURCE_URL = "jdbc:postgresql://${module.db.host_name}:${module.db.postgresql_listen_port}/${module.db.postgresql_database}?ssl=true"
    SPRING_DATASOURCE_USERNAME = "${module.db.user_name}"
    SPRING_DATASOURCE_PASSWORD = "${module.db.postgresql_password}"

    MAX_FILE_SIZE = "${var.max_file_size_in_mb}MB"

    # idam
    IDAM_USER_BASE_URI = "${var.idam_api_url}"
    IDAM_S2S_BASE_URI = "http://${var.s2s_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"

    # logging vars & healthcheck
    REFORM_SERVICE_NAME = "${local.app_full_name}"
    REFORM_TEAM = "${var.team_name}"
    REFORM_SERVICE_TYPE = "${var.app_language}"
    REFORM_ENVIRONMENT = "${var.env}"

    PACKAGES_NAME = "${local.app_full_name}"
    PACKAGES_PROJECT = "${var.team_name}"
    PACKAGES_ENVIRONMENT = "${var.env}"

    ROOT_APPENDER = "${var.root_appender}"
    JSON_CONSOLE_PRETTY_PRINT = "${var.json_console_pretty_print}"
    LOG_OUTPUT = "${var.log_output}"

    # addtional log
    ROOT_LOGGING_LEVEL = "${var.root_logging_level}"
    LOG_LEVEL_SPRING_WEB = "${var.log_level_spring_web}"
    LOG_LEVEL_DM = "${var.log_level_dm}"
    SHOW_SQL = "${var.show_sql}"

    ENDPOINTS_HEALTH_SENSITIVE = "${var.endpoints_health_sensitive}"
    ENDPOINTS_INFO_SENSITIVE = "${var.endpoints_info_sensitive}"

    ENABLE_DB_MIGRATE="false"

    DM_MULTIPART_WHITELIST = "${var.dm_multipart_whitelist}"
    DM_MULTIPART_WHITELIST_EXT = "${var.dm_multipart_whitelist_ext}"
    S2S_NAMES_WHITELIST = "${var.s2s_names_whitelist}"
    CASE_WORKER_ROLES = "${var.case_worker_roles}"

    # Toggles
    ENABLE_IDAM_HEALTH_CHECK = "${var.enable_idam_healthcheck}"
    ENABLE_METADATA_SEARCH = "${var.enable_metadata_search}"
    ENABLE_DOCUMENT_AND_METADATA_UPLOAD = "${var.enable_document_and_metadata_upload}"
    ENABLE_FOLDER_API = "${var.enable_folder_api}"
    ENABLE_DELETE = "${var.enable_delete}"
    ENABLE_TTL = "${var.enable_ttl}"
    ENABLE_THUMBNAIL = "${var.enable_thumbnail}"

    ENABLE_AZURE_STORAGE_CONTAINER = "${var.enable_azure_storage_container}"
    ENABLE_POSTGRES_BLOB_STORAGE = "${var.enable_postgres_blob_storage}"

    # Migration Job specific
    BLOBSTORE_MIGRATE_CCD_SECRET = "${var.blobstore_migrate_ccd_secret}"
    BLOBSTORE_MIGRATE_CCD_PUBLIC_KEY_REQUIRED = "${var.blobstore_migrate_ccd_public_key_required}"
    BLOBSTORE_MIGRATE_CCD_PUBLIC_KEY = "${var.blobstore_migrate_ccd_public_key}"

    # Document Storage
    STORAGEACCOUNT_PRIMARY_CONNECTION_STRING = "${data.azurerm_key_vault_secret.dm_store_storageaccount_primary_connection_string.value}"
    STORAGEACCOUNT_SECONDARY_CONNECTION_STRING = "${data.azurerm_key_vault_secret.dm_store_storageaccount_secondary_connection_string.value}"
    STORAGE_CONTAINER_DOCUMENT_CONTAINER_NAME = "${azurerm_storage_container.document_container.name}"
  }
}

module "db" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = "${local.app_full_name}-postgres-db"
  location = "${var.location}"
  env = "${var.env}"
  postgresql_user = "${var.postgresql_user}"
  database_name = "${var.database_name}"
  sku_name = "GP_Gen5_2"
  sku_tier = "GeneralPurpose"
  storage_mb = "51200"
  common_tags  = "${var.common_tags}"
}

resource "azurerm_storage_container" "document_container" {
  name = "${local.app_full_name}-docstore-${var.env}"
  resource_group_name = "${local.sharedResourceGroup}"
  storage_account_name = "${local.storageAccountNameDM}"
  container_access_type = "private"
}

data "azurerm_key_vault" "ccd_shared_vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.sharedResourceGroup}"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name = "${local.app_full_name}-POSTGRES-USER"
  value = "${module.db.user_name}"
  vault_uri = "${data.azurerm_key_vault.ccd_shared_vault.vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name = "${local.app_full_name}-POSTGRES-PASS"
  value = "${module.db.postgresql_password}"
  vault_uri = "${data.azurerm_key_vault.ccd_shared_vault.vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name = "${local.app_full_name}-POSTGRES-HOST"
  value = "${module.db.host_name}"
  vault_uri = "${data.azurerm_key_vault.ccd_shared_vault.vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name = "${local.app_full_name}-POSTGRES-PORT"
  value = "${module.db.postgresql_listen_port}"
  vault_uri = "${data.azurerm_key_vault.ccd_shared_vault.vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name = "${local.app_full_name}-POSTGRES-DATABASE"
  value = "${module.db.postgresql_database}"
  vault_uri = "${data.azurerm_key_vault.ccd_shared_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "dm_store_storageaccount_primary_connection_string" {
  name = "dm-store-storage-account-primary-connection-string"
  vault_uri = "${data.azurerm_key_vault.ccd_shared_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "dm_store_storageaccount_secondary_connection_string" {
  name = "dm-store-storage-account-secondary-connection-string"
  vault_uri = "${data.azurerm_key_vault.ccd_shared_vault.vault_uri}"
}
