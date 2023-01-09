package ee.ria.xtr_2_0.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CallerIdMissingException extends XtrException {

    public CallerIdMissingException(String[] callerIdKeys) {
        put("caller.idcode.keys", Arrays.toString(callerIdKeys));
    }

}
