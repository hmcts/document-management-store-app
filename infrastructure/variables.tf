variable "product" {
  type    = "string"
  default = "document-management-store-app"
}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "ssenv" {
  type = "string"
  default = "prd"
}

variable "ilbIp"{}
