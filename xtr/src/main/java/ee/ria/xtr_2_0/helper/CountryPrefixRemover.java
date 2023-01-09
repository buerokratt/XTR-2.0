package ee.ria.xtr_2_0.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for removing country codes
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CountryPrefixRemover {

    private static final String ID_CODE_PATTERN = "[a-zA-Z]{2}\\d{11}";

    /**
     * Strips country codes from parameter values if the key for such value is specified in the first
     * input (array of such keys) and the parameter value object is a String instance.
     * @param stripCountryPrefix contains keys for parameters that must have their values modified
     * @param parameters map of parameters
     * @return map of parameters with possibly some of them having their values modified (country code stripped)
     */
    public static Map<String, Object> stripCountryPrefix(String[] stripCountryPrefix, Map<String, Object> parameters) {
        Arrays.sort(stripCountryPrefix);
        return parameters.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            Object value = e.getValue();
            return mustStrip(e.getKey(), value, stripCountryPrefix) ? ((String) value).substring(2) : value;
        }));
    }

    private static boolean mustStrip(String key, Object value, String[] stripCountryPrefix) {
        return value instanceof String &&
                Arrays.binarySearch(stripCountryPrefix, key) > -1
                && ((String) value).matches(ID_CODE_PATTERN);
    }

}
