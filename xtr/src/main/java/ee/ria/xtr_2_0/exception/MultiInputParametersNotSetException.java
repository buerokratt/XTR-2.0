package ee.ria.xtr_2_0.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MultiInputParametersNotSetException extends XtrException {

    public MultiInputParametersNotSetException(String parameterName) {
        put("multi.input.param.name", parameterName);
    }

}
