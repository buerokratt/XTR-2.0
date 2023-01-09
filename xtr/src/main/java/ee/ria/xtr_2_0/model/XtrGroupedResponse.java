package ee.ria.xtr_2_0.model;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * Multiple responses as map.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class XtrGroupedResponse extends XtrResponse {

    private Map<Object, IntermediateConversionObject> response = Maps.newHashMap();

    public XtrGroupedResponse(XtrRequest request) {
        super(request);
    }

}
