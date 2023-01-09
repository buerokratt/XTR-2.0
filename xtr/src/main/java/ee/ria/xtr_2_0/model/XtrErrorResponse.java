package ee.ria.xtr_2_0.model;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class XtrErrorResponse {

    private final long timestamp = System.currentTimeMillis();

    private final int code;

    private final HttpStatus status;

    public XtrErrorResponse(HttpStatus status) {
        this.status = status;
        this.code = status.value();
    }
}
