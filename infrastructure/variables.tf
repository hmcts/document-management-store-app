variable product {
  default = "dm"
}

variable raw_product {
  default = "dm" // jenkins-library overrides product for PRs and adds e.g. pr-118-dm
}

variable shared_product {
  default = "ccd"
}

variable component {
  default = "store"
}

variable team_name {
  default = "evidence"
}

variable app_language {
  default = "java"
}

variable location {
  default = "UK South"
}

variable env {}

variable subscription {}

variable ilbIp {}

variable tenant_id {}

variable jenkins_AAD_objectId {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable deployment_namespace {}

////////////////////////////////////////////////
//Addtional Vars ///////////////////////////////
////////////////////////////////////////////////
variable capacity {
  default = "1"
}

variable java_opts {
  default = ""
}
////////////////////////////////////////////////
// Endpoints
////////////////////////////////////////////////
variable idam_api_url {
  default = "http://betaDevBccidamAppLB.reform.hmcts.net:80"
}

variable s2s_url {
  default = "rpe-service-auth-provider"
}

variable dm_store_app_url {
  default = "dm-store"
}

variable em_anno_app_url {
  default = "em-anno"
}

variable postgresql_user {
  default = "evidence"
}

variable database_name {
  default = "evidence"
}

variable postgresql_user_v11 {
  default = "evidence"
}

variable database_name_v11 {
  default = "evidence"
}

variable database_storage_mb {
  default     = "51200"
  description = "'storage_mb' size for the PaaS database (Note: can't be resized currently)"
}

////////////////////////////////////////////////
// Logging
////////////////////////////////////////////////

variable json_console_pretty_print {
  default = "false"
}

variable log_output {
  default = "single"
}

variable root_logging_level {
  default = "INFO"
}

variable log_level_spring_web {
  default = "INFO"
}

variable log_level_dm {
  default = "INFO"
}

variable show_sql {
  default = "true"
}

////////////////////////////////////////////////
// Toggle Features
////////////////////////////////////////////////
variable enable_idam_healthcheck {
  default = "false"
}

variable enable_metadata_search {
  default = "true"
}

variable enable_document_and_metadata_upload {
  default = "true"
}

variable enable_folder_api {
  default = "true"
}

variable enable_delete {
  default = "true"
}

variable enable_ttl {
  default = "false"
}

variable enable_thumbnail {
  default = "true"
}

variable enable_testing {
  default = "true"
}

variable enable_azure_media_service {
  default = false
}

variable sku_name {
  default = "GP_Gen5_2"
}

variable sku_capacity {
  default = "2"
}

////////////////////////////////////////////////
// Addtional
////////////////////////////////////////////////
variable max_file_size_in_mb {
  default = "100"
}

variable common_tags {
  type = map(string)
}
