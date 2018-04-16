variable "product" {
  type = "string"
}

variable "component" {
  type = "string"
}

variable "team_name" {
  default = "evidence"
}

variable "app_language" {
  default = "java"
}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "subscription" {
  type = "string"
}

variable "ilbIp"{}

variable "tenant_id" {}

variable "jenkins_AAD_objectId" {
  type                        = "string"
  description                 = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

////////////////////////////////////////////////
//Addtional Vars ///////////////////////////////
////////////////////////////////////////////////
variable "capacity" {
  default = "2"
}

variable "java_opts" {
  default = ""
}
////////////////////////////////////////////////
// Endpoints
////////////////////////////////////////////////
variable "vault_section" {
  default = "test"
}

variable "idam_api_url" {
  default = "http://betaDevBccidamAppLB.reform.hmcts.net:80"
}

variable "s2s_url" {
//  default = "http://betaDevBccidamS2SLB.reform.hmcts.net:80"
  default = "rpe-service-auth-provider"
}

variable "dm_store_app_url" {
  default = "dm-store"
}

variable "em_anno_app_url" {
  default = "em-anno"
}

variable "postgresql_user" {
  default = "evidence"
}

variable "database_name" {
  default = "evidence"
}
////////////////////////////////////////////////
// Logging
////////////////////////////////////////////////
variable "root_appender" {
  default = "JSON_CONSOLE"
}

variable "json_console_pretty_print" {
  default = "false"
}

variable "log_output" {
  default = "single"
}

variable "root_logging_level" {
  default = "INFO"
}

variable "log_level_spring_web" {
  default = "INFO"
}

variable "log_level_dm" {
  default = "INFO"
}

variable "show_sql" {
  default = "true"
}

variable "endpoints_health_sensitive" {
  default = "true"
}

variable "endpoints_info_sensitive" {
  default = "true"
}
////////////////////////////////////////////////
// Toggle Features
////////////////////////////////////////////////
variable "enable_idam_healthcheck" {
    default = "false"
}

variable "enable_metadata_search" {
    default = "true"
}

variable "enable_document_and_metadata_upload" {
    default = "true"
}

variable "enable_folder_api" {
    default = "true"
}

variable "enable_delete" {
    default = "true"
}

variable "enable_ttl" {
    default = "true"
}

variable "enable_thumbnail" {
    default = "true"
}
////////////////////////////////////////////////
// Whitelists
////////////////////////////////////////////////
variable "dm_multipart_whitelist" {
  default = "image/jpeg,application/pdf,image/tiff,image/png,image/bmp"
}

variable "dm_multipart_whitelist_ext" {
  default = ".jpg,.jpeg,.bmp,.tif,.tiff,.png,.pdf"
}

variable "s2s_names_whitelist" {
  default = "em_api,em_gw,ccd,sscs,divorce_document_upload,divorce_document_generator,probate_backend"
}

variable "case_worker_roles" {
  default = "caseworker-probate,caseworker-cmc,caseworker-sscs,caseworker-divorce"
}
////////////////////////////////////////////////
// Addtional
////////////////////////////////////////////////
variable "max_file_size_in_mb" {
    default = "500"
}
