java_opts = ""

////////////////////////////////////////////////
// Endpoints
////////////////////////////////////////////////
idam_api_url = "http://betaDevBccidamAppLB.reform.hmcts.net:80"

////////////////////////////////////////////////
// Logging
////////////////////////////////////////////////
root_appender = "JSON_CONSOLE"
json_console_pretty_print = "false"
log_output = "single"
root_logging_level = "INFO"
log_level_spring_web = "INFO"
log_level_dm = "INFO"
show_sql = "true"
endpoints_health_sensitive = "true"
endpoints_info_sensitive = "true"

////////////////////////////////////////////////
// Toggle Features
////////////////////////////////////////////////
enable_idam_healthcheck = "false"
enable_metadata_search = "true"
enable_document_and_metadata_upload = "true"
enable_folder_api = "true"
enable_delete = "true"
enable_ttl  = "false"
enable_thumbnail = "true"

////////////////////////////////////////////////
//// Whitelists
////////////////////////////////////////////////
//dm_multipart_whitelist = "image/jpeg,application/pdf,image/tiff,image/png,image/bmp"
//dm_multipart_whitelist_ext = ".jpg,.jpeg,.bmp,.tif,.tiff,.png,.pdf"
//s2s_names_whitelist = "em_api,em_gw,ccd,sscs,divorce_document_upload,divorce_document_generator,probate_backend"
//case_worker_roles = "caseworker-probate,caseworker-cmc,caseworker-sscs,caseworker-divorce"

////////////////////////////////////////////////
// Addtional
////////////////////////////////////////////////
max_file_size_in_mb = "500"
