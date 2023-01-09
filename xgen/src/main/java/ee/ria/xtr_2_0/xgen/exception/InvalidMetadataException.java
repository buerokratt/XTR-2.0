package ee.ria.xtr_2_0.xgen.exception;

import ee.ria.xtr_2_0.exception.XtrException;

public class InvalidMetadataException extends XtrException {

    public InvalidMetadataException(String metadataFilePath) {
        put("metadata.path", metadataFilePath);
    }

}
