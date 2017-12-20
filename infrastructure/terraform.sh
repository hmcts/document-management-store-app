#!/usr/bin/env bash

state_store_resource_group="contino-moj-tf-state"
state_store_storage_acccount="continomojtfstate"
bootstrap_state_storage_container="contino-moj-tfstate-container"
productEnvironment="local"
product="dm"

getCreds() {

    export ARM_SUBSCRIPTION_ID="$(azure keyvault secret show contino-devops terraform-creds --json | jq -r .value | jq -r .azure_subscription)"
    export ARM_CLIENT_ID="$(azure keyvault secret show contino-devops terraform-creds --json | jq -r .value | jq -r .azure_client_id)"
    export ARM_CLIENT_SECRET="$(azure keyvault secret show contino-devops terraform-creds --json | jq -r .value | jq -r .azure_client_secret)"
    export ARM_TENANT_ID="$(azure keyvault secret show contino-devops terraform-creds --json | jq -r .value | jq -r .azure_tenant_id)"

}


getCreds

terraform init \
    -backend-config "storage_account_name=$state_store_storage_acccount" \
    -backend-config "container_name=$bootstrap_state_storage_container" \
    -backend-config "resource_group_name=$state_store_resource_group" \
    -backend-config "key=$product/$productEnvironment/terraform.tfstate"

terraform "$@"
