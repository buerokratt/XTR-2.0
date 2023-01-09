package ee.ria.xtr_2_0.exception;


public class AttachmentMissingException extends XtrException {

    public AttachmentMissingException() {
        super("Response has no attachment");
    }

}
