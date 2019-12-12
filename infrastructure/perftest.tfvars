java_opts = ""

// Webapp
capacity = "2"
asp_name = "dm-store-perftest"
asp_rg = "dm-store-perftest"

// Database
database_storage_mb = "509952"

////////////////////////////////////////////////
// Endpoints
////////////////////////////////////////////////
idam_api_url = "https://idam-api.perftest.platform.hmcts.net"

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
// Migration Job Specific
////////////////////////////////////////////////
blobstore_migrate_ccd_public_key_required = "true"

////////////////////////////////////////////////
// Addtional
////////////////////////////////////////////////
max_file_size_in_mb = "500"
managed_identity_object_id="5cf9fbee-5b89-4664-b25d-39cb24e8684c"
