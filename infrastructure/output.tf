output "microserviceName" {
  value = "${var.product}-${var.app_name}-${var.app_type}"
}

output "vaultName" {
  value = "${module.key_vault.key_vault_name}"
}

output "vaultUri" {
  value = "${module.key_vault.key_vault_uri}"
}

output "idam_api_url" {
  value = "${var.idam_api_url}"
}

output "s2s_url" {
  value = "${var.s2s_url}"
}


//output "dm_store_app_url" {
//  value = "http://${var.dm_store_app_url}-${var.env}.service.${local.ase_name}.internal"
//}
//
//output "em_anno_app_url" {
//  value = "http://${var.em_anno_app_url}-${var.env}.service.${local.ase_name}.internal"
//}
