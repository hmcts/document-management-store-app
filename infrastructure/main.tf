module "recipe-backend" {
  source   = "git@github.com:contino/moj-module-webapp?ref=private-ase"
  product  = "${var.product}-recipe-backend"
  location = "${var.location}"
  env      = "${var.env}"
  ilbIp    = "${var.ilbIp}"


  app_settings = {
    POSTGRES_HOST     = "${module.recipe-database.host_name}"
    POSTGRES_PORT     = "${module.recipe-database.postgresql_listen_port}"
    POSTGRES_DATABASE = "${module.recipe-database.postgresql_database}"
    POSTGRES_USER     = "${module.recipe-database.user_name}"
    POSTGRES_PASSWORD = "Password1"
  }
}

#module "redis-cache" {
#  source   = "git@github.com:contino/moj-module-redis?ref=master"
#  product  = "${var.product}"
#  location = "${var.location}"
#  env      = "${var.env}"
#  subnetid = "${data.terraform_remote_state.core_apps_infrastructure.subnet_ids[1]}"
#}

module "recipe-database" {
  source              = "git@github.com:contino/moj-module-postgres?ref=master"
  product             = "${var.product}-combined"
  location            = "West Europe"
  env                 = "${var.env}"
  postgresql_user     = "rhubarbadmin"
  postgresql_password = "Password1"
}
