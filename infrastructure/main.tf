module "document-management-store-app" {
    source   = "git@github.com:contino/moj-module-webapp?ref=master"
    product  = "${var.product}-document-management-store-app"
    location = "${var.location}"
    env      = "${var.env}"
    ilbIp    = "${var.ilbIp}"

    app_settings = {
        POSTGRES_HOST     = "${module.document-management-store-postgres-db.host_name}"
        POSTGRES_PORT     = "${module.document-management-store-postgres-db.postgresql_listen_port}"
        POSTGRES_DATABASE = "${module.document-management-store-postgres-db.postgresql_database}"
        POSTGRES_USER     = "${module.document-management-store-postgres-db.user_name}"
        POSTGRES_PASSWORD = "${module.document-management-store-postgres-db.postgresql_password}"
    }
}


module "document-management-store-postgres-db" {
    source              = "git@github.com:contino/moj-module-postgres?ref=master"
    product             = "${var.product}-document-management-store-postgres-db"
    location            = "West Europe"
    env                 = "${var.env}"
    postgresql_user     = "rhubarbadmin"
}

module "key-vault" {
    source              = "git@github.com:contino/moj-module-key-vault?ref=master"
    product             = "${var.product}"
    env                 = "${var.env}"
    tenant_id           = "${var.tenant_id}"
    object_id           = "${var.jenkins_AAD_objectId}"
    resource_group_name = "${module.document-management-store-app.resource_group_name}"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
    name      = "recipe-backend-POSTGRES-USER"
    value     = "${module.document-management-store-postgres-db.user_name}"
    vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
    name      = "recipe-backend-POSTGRES-PASS"
    value     = "${module.document-management-store-postgres-db.postgresql_password}"
    vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
    name      = "recipe-backend-POSTGRES-HOST"
    value     = "${module.document-management-store-postgres-db.host_name}"
    vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
    name      = "recipe-backend-POSTGRES-PORT"
    value     = "${module.document-management-store-postgres-db.postgresql_listen_port}"
    vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
    name      = "recipe-backend-POSTGRES-DATABASE"
    value     = "${module.document-management-store-postgres-db.postgresql_database}"
    vault_uri = "${module.key-vault.key_vault_uri}"
}
