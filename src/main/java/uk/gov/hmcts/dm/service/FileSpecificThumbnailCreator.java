package uk.gov.hmcts.dm.service;

public interface FileSpecificThumbnailCreator extends ThumbnailCreator{
    boolean supports(String mimeType);

}
