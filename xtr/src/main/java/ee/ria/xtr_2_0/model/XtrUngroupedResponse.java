package ee.ria.xtr_2_0.model;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * Multiple responses as collection.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class XtrUngroupedResponse extends XtrResponse {

    private Collection<IntermediateConversionObject> response = Lists.newArrayList();

    public XtrUngroupedResponse(XtrRequest request) {
        super(request);
    }

}
