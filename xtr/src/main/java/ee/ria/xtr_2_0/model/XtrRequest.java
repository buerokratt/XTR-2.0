package ee.ria.xtr_2_0.model;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class XtrRequest {

    /**
     * @see XtrDatabase#registryCode
     */
    @NonNull
    private String register;

    /**
     * @see XtrDatabase#serviceCode
     */
    @NonNull
    private String service;

    private String[] stripCountryPrefix;

    private Map<String, Object> parameters = Maps.newHashMap();

    private String multipleInputsFrom;

    private String groupResponseByField;

}
