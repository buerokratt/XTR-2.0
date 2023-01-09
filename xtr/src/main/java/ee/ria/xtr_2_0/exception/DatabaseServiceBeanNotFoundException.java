package ee.ria.xtr_2_0.exception;

public class DatabaseServiceBeanNotFoundException extends XtrException {

    public DatabaseServiceBeanNotFoundException(String serviceName) {
        this(serviceName, null);
    }

    public DatabaseServiceBeanNotFoundException(String serviceName, Throwable cause) {
        super(cause);
        put("service.name", serviceName);
    }

}
