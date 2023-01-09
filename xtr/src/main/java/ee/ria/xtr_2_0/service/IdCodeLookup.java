package ee.ria.xtr_2_0.service;

import java.util.Map;

/**
 * Defines a method for finding Id code String
 */
public interface IdCodeLookup {

    String findIdCode(Map<String, Object> parameters);

}
