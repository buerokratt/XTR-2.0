package ee.ria.xtr_2_0.service;

import com.google.common.collect.Maps;
import ee.ria.xtr_2_0.exception.MultiInputParametersNotSetException;
import ee.ria.xtr_2_0.helper.CountryPrefixRemover;
import ee.ria.xtr_2_0.model.XtrDatabase;
import ee.ria.xtr_2_0.model.XtrRequest;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for MethodExecutors
 * @see MethodExecutor
 */
public class MethodExecutorFactory {

    private final XtrRequest request;
    private final XtrDatabase database;

    private final boolean isStrip;
    private final boolean isMultiInput;

    /**
     * Constructor creates new factory instance with enough data to later construct the most suitable MethodExecutor
     * instances for given request     *
     * @param request to be performed
     * @param database  contains mapping  between request and service, as well as other relevant config
     */
    public MethodExecutorFactory(XtrRequest request, XtrDatabase database) {
        this.request = request;
        this.database = database;
        this.isStrip = request.getStripCountryPrefix() != null && request.getStripCountryPrefix().length > 0;
        this.isMultiInput = !StringUtils.isEmpty(request.getMultipleInputsFrom());
    }

    /**
     *
     * @return MethodExecutor returned depends on the parameters that were used in constructing this MethodExecutorFactory
     * instance
     */
    public MethodExecutor executor() {
        if (!isStrip && !isMultiInput) {
            return MethodExecutor.factory(request, database).withParams(request.getParameters()).build();
        }

        // don't want to modify existing parameters map
        // create a new one
        Map<String, Object> result = isStrip ?
                CountryPrefixRemover.stripCountryPrefix(request.getStripCountryPrefix(), request.getParameters())
                : Maps.newHashMap(request.getParameters());

        return isMultiInput ? divideParameters(request.getMultipleInputsFrom(), result)
                : MethodExecutor.factory(request, database).withParams(result).build();
    }

    private MethodExecutor divideParameters(String multipleInputsFrom, Map<String, Object> params) {
        Object multiParams = params.get(multipleInputsFrom);
        if (multiParams == null) {
            throw new MultiInputParametersNotSetException(multipleInputsFrom);
        }

        Object[] multiParamsArray;
        // collection
        if (multiParams instanceof Collection) {
            multiParamsArray = ((Collection<?>) multiParams).toArray(new Object[0]);
        }
        // array
        else if (multiParams.getClass().isArray()) {
            multiParamsArray = (Object[]) multiParams;
        }
        // single element
        else {
            multiParamsArray = new Object[] { multiParams };
        }

        final MethodExecutor.Factory factory = MethodExecutor.factory(request, database);
        Arrays.stream(multiParamsArray).forEach(singleValue -> {
            HashMap<String, Object> parameters = Maps.newHashMap(params);
            // replace with single value
            parameters.put(multipleInputsFrom, singleValue);
            factory.withParams(parameters);
        });

        return factory.build();
    }

}
