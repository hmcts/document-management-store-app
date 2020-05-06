package uk.gov.hmcts.reform.dm.config;

import org.springframework.http.MediaType;

public class V1MediaType extends MediaType {

    public static final String V1_HAL_DOCUMENT_AND_METADATA_COLLECTION_MEDIA_TYPE_VALUE = "application/vnd.uk.gov.hmcts.reform.dm.document-and-metadata-collection.v1+hal+json;charset=UTF-8";

    public static final MediaType V1_HAL_DOCUMENT_AND_METADATA_COLLECTION_MEDIA_TYPE = valueOf(V1_HAL_DOCUMENT_AND_METADATA_COLLECTION_MEDIA_TYPE_VALUE);

    public static final String V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE_VALUE = "application/vnd.uk.gov.hmcts.reform.dm.document-page.v1+hal+json;charset=UTF-8";

    public static final MediaType V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE = valueOf(V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE_VALUE);

    public static final String V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE = "application/vnd.uk.gov.hmcts.reform.dm.document-collection.v1+hal+json;charset=UTF-8";

    public static final MediaType V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE = valueOf(V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE);

    public static final String V1_HAL_DOCUMENT_MEDIA_TYPE_VALUE = "application/vnd.uk.gov.hmcts.reform.dm.document.v1+hal+json;charset=UTF-8";

    public static final MediaType V1_HAL_DOCUMENT_MEDIA_TYPE = valueOf(V1_HAL_DOCUMENT_MEDIA_TYPE_VALUE);

    public static final String V1_HAL_FOLDER_MEDIA_TYPE_VALUE = "application/vnd.uk.gov.hmcts.reform.dm.folder.v1+hal+json;charset=UTF-8";

    public static final MediaType V1_HAL_FOLDER_MEDIA_TYPE = valueOf(V1_HAL_FOLDER_MEDIA_TYPE_VALUE);

    public static final String V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE = "application/vnd.uk.gov.hmcts.reform.dm.documentContentVersion.v1+hal+json;charset=UTF-8";

    public static final MediaType V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE = valueOf(V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE);

    public static final String V1_HAL_AUDIT_ENTRY_MEDIA_TYPE_VALUE = "application/vnd.uk.gov.hmcts.reform.dm.auditEntry.v1+hal+json;charset=UTF-8";

    public static final MediaType V1_HAL_AUDIT_ENTRY_MEDIA_TYPE = valueOf(V1_HAL_AUDIT_ENTRY_MEDIA_TYPE_VALUE);

    public static final String V1_HAL_AUDIT_ENTRY_COLLECTION_MEDIA_TYPE_VALUE = "application/vnd.uk.gov.hmcts.reform.dm.auditEntry-collection.v1+hal+json;charset=UTF-8";

    public static final MediaType V1_HAL_AUDIT_ENTRY_COLLECTION_MEDIA_TYPE = valueOf(V1_HAL_AUDIT_ENTRY_COLLECTION_MEDIA_TYPE_VALUE);


    public static final String V1_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE = "application/vnd.uk.gov.hmcts.reform.dm.document-collection.v1+json;charset=UTF-8";

    public static final MediaType V1_DOCUMENT_COLLECTION_MEDIA_TYPE = valueOf(V1_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE);

    public static final String V1_DOCUMENT_MEDIA_TYPE_VALUE = "application/vnd.uk.gov.hmcts.reform.dm.document.v1+json;charset=UTF-8";

    public static final MediaType V1_DOCUMENT_MEDIA_TYPE = valueOf(V1_DOCUMENT_MEDIA_TYPE_VALUE);

    public static final String V1_FOLDER_MEDIA_TYPE_VALUE = "application/vnd.uk.gov.hmcts.reform.dm.folder.v1+json;charset=UTF-8";

    public static final MediaType V1_FOLDER_MEDIA_TYPE = valueOf(V1_FOLDER_MEDIA_TYPE_VALUE);

    public static final String V1_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE = "application/vnd.uk.gov.hmcts.reform.dm.documentContentVersion.v1+json;charset=UTF-8";

    public static final MediaType V1_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE = valueOf(V1_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE);

    public static final String V1_AUDIT_ENTRY_MEDIA_TYPE_VALUE = "application/vnd.uk.gov.hmcts.reform.dm.auditEntry.v1+json;charset=UTF-8";

    public static final MediaType V1_AUDIT_ENTRY_MEDIA_TYPE = valueOf(V1_AUDIT_ENTRY_MEDIA_TYPE_VALUE);

    public V1MediaType(String type, String subtype) {
        super(type, subtype);
    }



}
