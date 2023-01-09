package ee.ria.xtr_2_0.exception;

public class ServiceMethodNotDistinguishableException extends XtrException {

    public ServiceMethodNotDistinguishableException(String serviceName, String method) {
        super(serviceName + "." + method);
        put("service.name", serviceName);
        put("service.method", method);
    }

}
