package ee.ria.xtr_2_0.exception;

import ee.ria.xtr_2_0.model.XtrAttachment;

public class AttachmentCreationException extends XtrException {

    public AttachmentCreationException(String message) {
        super(message);
    }

    public AttachmentCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AttachmentCreationException(String message, XtrAttachment attachmentConf) {
        super(message);
        put("attachment.conf", attachmentConf);
    }

}
