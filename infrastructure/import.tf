locals {
  docstore_import_ids = {
    aat  = "https://dmstoredocaat.blob.core.windows.net/dm-store-docstore-aat"
    prod = "https://dmstoredocprod.blob.core.windows.net/dm-store-docstore-prod"
  }
}

import {
  for_each = try(toset([local.docstore_import_ids[var.env]]), toset([]))
  to       = azurerm_storage_container.document_container
  id       = each.value
}

