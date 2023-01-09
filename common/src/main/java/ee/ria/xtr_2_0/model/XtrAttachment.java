package ee.ria.xtr_2_0.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class XtrAttachment {

    private Map<String, Object> request;

    private Map<String, Object> response;

}
