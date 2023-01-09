package ee.ria.xtr_2_0.exception;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public abstract class XtrException extends RuntimeException {

    @Getter
    private Map<String, Object> data = Maps.newHashMap();

    public XtrException(String message) {
        super(message);
    }

    public XtrException(Throwable cause) {
        super(cause);
    }

    public XtrException(String message, Throwable cause) {
        super(message, cause);
    }

    protected void put(String key, Object data) {
        this.data.put(key, data);
    }

}
