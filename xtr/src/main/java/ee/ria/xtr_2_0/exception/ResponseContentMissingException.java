package ee.ria.xtr_2_0.exception;


public class ResponseContentMissingException extends XtrException {

    public ResponseContentMissingException() {
        super("Response has no content");
    }

}
