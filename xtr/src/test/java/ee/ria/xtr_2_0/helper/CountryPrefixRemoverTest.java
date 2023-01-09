package ee.ria.xtr_2_0.helper;

import com.google.common.collect.Maps;
import ee.ria.xtr_2_0.model.XtrRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CountryPrefixRemoverTest {

    @Test
    void ifStripCountryPrefIsSetThenStripCountryPrefixes() {
        String[] toStrip = new String[]{
                "foo", "bar"
        };

        Map<String, Object> params = Maps.newHashMap();
        params.put("foo", "EE00000000001");
        params.put("asd", "EE00000000000");
        params.put("bar", "EE00000000002");

        XtrRequest request = new XtrRequest();
        request.setStripCountryPrefix(toStrip);
        request.setParameters(params);

        Map<String, Object> stripped = CountryPrefixRemover.stripCountryPrefix(
                request.getStripCountryPrefix(), request.getParameters());

        assertThat(stripped).containsValues("EE00000000000", "00000000001", "00000000002");
    }

}
