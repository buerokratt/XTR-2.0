package ee.ria.xtr_2_0.exception;

public class ParameterFactoryNotFoundException extends XtrException {

    public ParameterFactoryNotFoundException(Class<?> parameterClass) {
        put("parameter.class", parameterClass.getName());
    }

}
