variable "product" {
  type = "string"
}

variable "raw_product" {
  default = "dm" // jenkins-library overrides product for PRs and adds e.g. pr-118-dm
}

variable "shared_product" {
  default = "ccd"
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
  default = "1"
}

variable "java_opts" {
  default = ""
}
////////////////////////////////////////////////
// Endpoints
////////////////////////////////////////////////
variable "idam_api_url" {
  default = "http://betaDevBccidamAppLB.reform.hmcts.net:80"
}

variable "s2s_url" {
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

variable "database_storage_mb" {
  default = "51200"
  description = "'storage_mb' size for the PaaS database (Note: can't be resized currently)"
}

////////////////////////////////////////////////
// Logging
////////////////////////////////////////////////

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
    default = "false"
}

variable "enable_thumbnail" {
    default = "true"
}

variable "enable_testing" {
  default = "true"
}

////////////////////////////////////////////////
// Migration Job Specific
////////////////////////////////////////////////
variable "blobstore_migrate_ccd_secret" {
  default = "y2hahvdZ9evcTVq2"
}

variable "blobstore_migrate_ccd_public_key_required" {
  default = "false"
}

variable "blobstore_migrate_ccd_public_key" {
  default = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDiQ//gc/G53d9dLCtf123fIYo49gUySuJuxOcw2GtieWTMSy+O7RNtsAIjVf3mCOdDNuN69tZNPEWMdaW8n11s9MwYFahtxDecyn0KIP9MvPsfSMSbxhp/f7kfbdB/H/S5eYea66JTyeJS6uNd76RdHttx0mLO30ZkRcXB25c2SIXhRYsdoeKS5GXHDdNejkQM0S/Ev94x2UunApmYHjWN1XcDhsEsAeF4WHnvYh2XiMn9vHY44AqvbWLlAmCgzaXpz8Xhl0fO7jDKSeReDyuM3UTMaiFFaxuvliGol7aIXq/aVe/miiD2SLxHZ6RxAPW80bhXrzJMTLTCqhCEhzfv someone@somewhere.sometime"
}

////////////////////////////////////////////////
// Whitelists
////////////////////////////////////////////////
variable "dm_multipart_whitelist" {
  default = "image/jpeg,application/pdf,image/tiff,image/png,image/bmp,text/plain,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.openxmlformats-officedocument.wordprocessingml.template,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.openxmlformats-officedocument.spreadsheetml.template,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,application/vnd.openxmlformats-officedocument.presentationml.template,application/vnd.openxmlformats-officedocument.presentationml.slideshow,application/rtf,text/csv"
}

variable "dm_multipart_whitelist_ext" {
  default = ".jpg,.jpeg,.bmp,.tif,.tiff,.png,.pdf,.txt,.doc,.dot,.docx,.dotx,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.rtf,.csv"
}

////////////////////////////////////////////////
// Addtional
////////////////////////////////////////////////
variable "max_file_size_in_mb" {
    default = "100"
}

variable "common_tags" {
  type = "map"
}

variable "asp_name" {
  type = "string"
  description = "App Service Plan (ASP) to use for the webapp, 'use_shared' to make use of the shared ASP"
  default = "use_shared"
}

variable "asp_rg" {
  type = "string"
  description = "App Service Plan (ASP) resource group for 'asp_name', 'use_shared' to make use of the shared resource group"
  default = "use_shared"
}

variable "managed_identity_object_id" {
  default = ""
}
