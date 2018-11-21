output "microserviceName" {
  value = "${var.product}-${var.component}"
}

output "vaultName" {
  value = "${local.vaultName}"
}

output "vaultUri" {
  value = "${data.azurerm_key_vault.dm_shared_vault.vault_uri}"
}

output "idam_api_url" {
  value = "${var.idam_api_url}"
}

output "s2s_url" {
  value = "http://${var.s2s_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"
}

output "dm_store_app_url" {
  value = "http://${var.dm_store_app_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"
}

output "em_anno_app_url" {
  value = "http://${var.em_anno_app_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"
}

output "enable_idam_health_check" {
  value = "${var.enable_idam_healthcheck}"
}

output "enable_metadata_search" {
  value = "${var.enable_metadata_search}"
}

output "enable_document_and_metadata_upload" {
  value = "${var.enable_document_and_metadata_upload}"
}

output "enable_folder_api" {
  value = "${var.enable_folder_api}"
}

output "enable_delete" {
  value = "${var.enable_delete}"
}

output "enable_ttl" {
  value = "${var.enable_ttl}"
}

output "enable_thumbnail" {
  value = "${var.enable_thumbnail}"
}

