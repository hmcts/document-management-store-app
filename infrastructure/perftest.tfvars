java_opts = ""

////////////////////////////////////////////////
// Endpoints
////////////////////////////////////////////////
idam_api_url = "https://idam-api.perftest.platform.hmcts.net"

////////////////////////////////////////////////
// Logging
////////////////////////////////////////////////
json_console_pretty_print = "false"
log_output                = "single"
root_logging_level        = "INFO"
log_level_spring_web      = "INFO"
log_level_dm              = "INFO"
show_sql                  = "false"

////////////////////////////////////////////////
// Toggle Features
////////////////////////////////////////////////
enable_idam_healthcheck             = "false"
enable_metadata_search              = "true"
enable_document_and_metadata_upload = "false"
enable_folder_api                   = "true"
enable_ttl                          = "false"
enable_thumbnail                    = "true"

////////////////////////////////////////////////
// Addtional
////////////////////////////////////////////////
max_file_size_in_mb = "500"

//v15 Flexiserver DB
pgsql_sku        = "MO_Standard_E16ds_v4"
pgsql_storage_mb = "524288"
