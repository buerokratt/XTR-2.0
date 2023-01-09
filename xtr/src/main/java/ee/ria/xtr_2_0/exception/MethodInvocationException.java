package ee.ria.xtr_2_0.exception;

import java.lang.reflect.Method;

public class MethodInvocationException extends XtrException {

    public MethodInvocationException(Method method, Throwable cause) {
        super(cause);
        put("method.name", method.getName());
    }

}
