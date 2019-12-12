java_opts = ""

// Webapp
capacity = "2"
asp_name = "dm-store-aat"
asp_rg = "dm-store-aat"

// Database
database_storage_mb = "100352"

////////////////////////////////////////////////
// Endpoints
////////////////////////////////////////////////
#idam_api_url = "https://preprod-idamapi.reform.hmcts.net:3511"
idam_api_url = "https://idam-api.aat.platform.hmcts.net"

////////////////////////////////////////////////
// Logging
////////////////////////////////////////////////
json_console_pretty_print = "false"
log_output = "single"
root_logging_level = "INFO"
log_level_spring_web = "INFO"
log_level_dm = "INFO"
show_sql = "false"

////////////////////////////////////////////////
// Toggle Features
////////////////////////////////////////////////
enable_idam_healthcheck = "false"
enable_metadata_search = "true"
enable_document_and_metadata_upload = "false"
enable_folder_api = "true"
enable_delete = "true"
enable_ttl  = "false"
enable_thumbnail = "true"

////////////////////////////////////////////////
// Addtional
////////////////////////////////////////////////
max_file_size_in_mb = "100"
managed_identity_object_id="a1b6213f-9492-4199-989b-0701059976df"
