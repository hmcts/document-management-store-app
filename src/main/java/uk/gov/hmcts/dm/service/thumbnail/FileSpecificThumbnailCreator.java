package uk.gov.hmcts.dm.service.thumbnail;

public interface FileSpecificThumbnailCreator extends ThumbnailCreator {

    boolean supports(String mimeType);

}
