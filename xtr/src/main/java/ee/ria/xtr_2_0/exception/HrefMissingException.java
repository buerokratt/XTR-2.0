package ee.ria.xtr_2_0.exception;


public class HrefMissingException extends XtrException {

    public HrefMissingException(String hrefPath, Object o) {
        this(hrefPath, o, null);
    }

    public HrefMissingException(String hrefPath, Object o, Throwable cause) {
        super("Variable 'href' missing or is not set", cause);
        put("href.path", hrefPath);
        put("response.content", o);
    }

}
