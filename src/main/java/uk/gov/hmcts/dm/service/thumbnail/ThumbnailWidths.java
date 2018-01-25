package uk.gov.hmcts.dm.service.thumbnail;

import lombok.Getter;

/**
 * Created by pawel on 25/01/2018.
 */
public enum ThumbnailWidths {

    WIDTH_256(256);

    @Getter
    private int width;

    ThumbnailWidths(int width) {
        this.width = width;
    }
}
