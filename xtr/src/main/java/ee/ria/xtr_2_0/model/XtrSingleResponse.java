package ee.ria.xtr_2_0.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Single response.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class XtrSingleResponse extends XtrResponse {

    private IntermediateConversionObject response;

    public XtrSingleResponse(XtrRequest request, IntermediateConversionObject response) {
        super(request);
        this.response = response;
    }
}
