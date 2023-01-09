package ee.ria.xtr_2_0.xgen.util;

import com.nortal.jroad.model.XmlBeansXRoadMetadata;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodNameUtilTest {

    @Test
    void useVersionIfItExists() {
        assertThat(MethodNameUtil.methodName(new XmlBeansXRoadMetadata("foo", null, null, null, null, null, "v1")))
                .isEqualTo("fooV1");
    }

    @Test
    void doNotUseVersionIfItDoesNotExist() {
        assertThat(MethodNameUtil.methodName(new XmlBeansXRoadMetadata("FooBar", null, null, null, null, null, null)))
                .isEqualTo("fooBar");
    }

}
