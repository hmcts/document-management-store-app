variable "product" {
    type    = "string"
    default = "dm"
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

//Addtional Vars

variable "idam-api-url" {
    default = "http://betaDevBccidamAppLB.reform.hmcts.net:4551"
}

variable "s2s-url" {
    default = "http://betaDevBccidamAppLB.reform.hmcts.net:4552"
}
