locals {
  app_full_name = "${var.product}-${var.component}"
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
}
# "${local.ase_name}"
# "${local.app_full_name}"
# "${local.local_env}"

module "app" {
  source = "git@github.com:hmcts/moj-module-webapp?ref=master"
  product = "${local.app_full_name}"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"
  subscription = "${var.subscription}"
  capacity     = "${var.capacity}"
  is_frontend = false
  additional_host_name = "${local.app_full_name}-${var.env}.service.${var.env}.platform.hmcts.net"
  https_only="false"

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
  }
}

module "db" {
  source = "git@github.com:hmcts/moj-module-postgres?ref=cnp-449-tactical"
  product = "${local.app_full_name}-postgres-db"
  location = "${var.location}"
  env = "${var.env}"
  postgresql_user = "${var.postgresql_user}"
  database_name = "${var.database_name}"
  sku_name = "GP_Gen5_2"
  sku_tier = "GeneralPurpose"
  storage_mb = "51200"
}

provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "s2s_secret" {
  path = "secret/${var.vault_section}/ccidam/service-auth-provider/api/microservice-keys/em-gw"
}

module "key_vault" {
  source = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  product = "${local.app_full_name}"
  env = "${var.env}"
  tenant_id = "${var.tenant_id}"
  object_id = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.app.resource_group_name}"
  product_group_object_id = "5d9cd025-a293-4b97-a0e5-6f43efce02c0"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name = "${local.app_full_name}-POSTGRES-USER"
  value = "${module.db.user_name}"
  vault_uri = "${module.key_vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name = "${local.app_full_name}-POSTGRES-PASS"
  value = "${module.db.postgresql_password}"
  vault_uri = "${module.key_vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name = "${local.app_full_name}-POSTGRES-HOST"
  value = "${module.db.host_name}"
  vault_uri = "${module.key_vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name = "${local.app_full_name}-POSTGRES-PORT"
  value = "${module.db.postgresql_listen_port}"
  vault_uri = "${module.key_vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name = "${local.app_full_name}-POSTGRES-DATABASE"
  value = "${module.db.postgresql_database}"
  vault_uri = "${module.key_vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "S2S_TOKEN" {
  name = "s2s-token"
  value = "${data.vault_generic_secret.s2s_secret.data["value"]}"
  vault_uri = "${module.key_vault.key_vault_uri}"
}

