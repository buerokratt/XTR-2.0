package ee.ria.xtr_2_0;

import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {

    public static MapInitializer map() {
        return new MapInitializer();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MapInitializer {

        private final Map<String, Object> map = Maps.newHashMap();

        public MapInitializer withEntry(String key, Object value) {
            map.put(key, value);
            return this;
        }

        public Map<String, Object> get() {
            return map;
        }

    }

}
