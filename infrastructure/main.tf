module "app" {
    source   = "git@github.com:contino/moj-module-webapp?ref=master"
    product  = "${var.product}-${var.app_name}-${var.app_type}"
    location = "${var.location}"
    env      = "${var.env}"
    ilbIp    = "${var.ilbIp}"

    app_settings = {
        POSTGRES_HOST     = "${module.db.host_name}"
        POSTGRES_PORT     = "${module.db.postgresql_listen_port}"
        POSTGRES_DATABASE = "${module.db.postgresql_database}"
        POSTGRES_USER     = "${module.db.user_name}"
        POSTGRES_PASSWORD = "${module.db.postgresql_password}"


        JAVA_OPTS =  "${var.java_opts}"
        SERVER_PORT = "8080"

        // db
        SPRING_DATASOURCE_URL = "jdbc:postgresql://${module.db.host_name}:${module.db.postgresql_listen_port}/${module.db.postgresql_database}"
        SPRING_DATASOURCE_USERNAME = "${module.db.user_name}"
        SPRING_DATASOURCE_PASSWORD = "${module.db.postgresql_password}"

        MAX_FILE_SIZE = "${var.max_file_size_in_mb}MB"

        // idam
        IDAM_CLIENT_URL = "${var.idam-api-url}"
        PROVIDER_SERVICE_CLIENT_URL = "${var.s2s-url}"


        //   logging vars & healthcheck
        REFORM_SERVICE_NAME = "${var.product}-${var.app_name}-${var.app_type}"
        REFORM_TEAM = "${var.team_name}"
        REFORM_SERVICE_TYPE = "${var.app_language}"
        REFORM_ENVIRONMENT = "${var.env}"

        PACKAGES_NAME = "${var.product}-${var.app_name}-${var.app_type}"
        PACKAGES_PROJECT = "${var.team_name}"
        PACKAGES_ENVIRONMENT = "${var.env}"

        ROOT_APPENDER =  "${var.root_appender}"
        JSON_CONSOLE_PRETTY_PRINT =  "${var.json_console_pretty_print}"
        LOG_OUTPUT =  "${var.log_output}"
        ROOT_LOGGING_LEVEL =  "${var.root_logging_level}"
        LOG_LEVEL_SPRING_WEB =  "${var.log_level_spring_web}"
        LOG_LEVEL_DM = "${var.log_level_dm}"
        SHOW_SQL = "${var.show_sql}"

        ENDPOINTS_HEALTH_SENSITIVE = "${var.endpoints_health_sensitive}"
        ENDPOINTS_INFO_SENSITIVE = "${var.endpoints_info_sensitive}"

//        DM_MULTIPART_WHITELIST = "${var.dm_multipart_whitelist}"
//        S2S_NAMES_WHITELIST = "${var.s2s_names_whitelist}"
//        CASE_WORKER_ROLES = "${var.case_worker_roles}"

//        Toggles
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
    source              = "git@github.com:contino/moj-module-postgres?ref=master"
    product             = "${var.product}-${var.app_name}-postgres-db"
    location            = "West Europe"
    env                 = "${var.env}"
    postgresql_user     = "rhubarbadmin"
}

module "key-vault" {
    source              = "git@github.com:contino/moj-module-key-vault?ref=master"
    product             = "${var.product}-${var.app_name}-${var.app_type}"
    env                 = "${var.env}"
    tenant_id           = "${var.tenant_id}"
    object_id           = "${var.jenkins_AAD_objectId}"
    resource_group_name = "${module.app.resource_group_name}"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
    name      = "${var.product}-${var.app_name}-POSTGRES-USER"
    value     = "${module.db.user_name}"
    vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
    name      = "${var.product}-${var.app_name}-POSTGRES-PASS"
    value     = "${module.db.postgresql_password}"
    vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
    name      = "${var.product}-${var.app_name}-POSTGRES-HOST"
    value     = "${module.db.host_name}"
    vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
    name      = "${var.product}-${var.app_name}-POSTGRES-PORT"
    value     = "${module.db.postgresql_listen_port}"
    vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
    name      = "${var.product}-${var.app_name}-POSTGRES-DATABASE"
    value     = "${module.db.postgresql_database}"
    vault_uri = "${module.key-vault.key_vault_uri}"
}
