package ee.ria.xtr_2_0.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Abstract class defining the generic structure for returning X-Road responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class XtrResponse {

    private XtrRequest request;

}
