package ee.ria.xtr_2_0.service.attachment;

import com.nortal.jroad.model.XRoadAttachment;
import ee.ria.xtr_2_0.TestUtils;
import ee.ria.xtr_2_0.helper.Constants;
import ee.ria.xtr_2_0.service.attachment.BaseAttachmentBuilder.ContentModifier;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BaseAttachmentBuilderTest {

    private static final String INPUT = "test string";

    @Test
    void testNoModifiers() {
        assertThat(new TestAttachmentBuilder().getBytes(INPUT)).containsExactly(
                new byte[] { 116, 101, 115, 116, 32, 115, 116, 114, 105, 110, 103 }
        );
    }

    @Test
    void testBase64() {
        assertThat(new TestAttachmentBuilder(ContentModifier.BASE64).getBytes(INPUT))
                .containsExactly(
                        new byte[] { 100, 71, 86, 122, 100, 67, 66, 122, 100, 72, 74, 112, 98, 109, 99, 61 }
                );
    }

    @Test
    void testGzip() {
        assertThat(new TestAttachmentBuilder(ContentModifier.GZIP).getBytes(INPUT))
                .containsExactly(
                        new byte[] {
                                31, -117, 8, 0, 0, 0, 0, 0, 0, 0, 43, 73, 45, 46, 81, 40,
                                46, 41, -54, -52, 75, 7, 0, 69, 21, 71, 19, 11, 0, 0, 0
                        }
                );
    }

    @Test
    void testBase64AndGzip() {
        assertThat(new TestAttachmentBuilder(ContentModifier.BASE64, ContentModifier.GZIP).getBytes(INPUT))
                .containsExactly(new byte[] {
                        72, 52, 115, 73, 65, 65, 65, 65, 65, 65, 65, 65, 65, 67, 116,
                        74, 76, 83, 53, 82, 75, 67, 52, 112, 121, 115, 120, 76, 66,
                        119, 66, 70, 70, 85, 99, 84, 67, 119, 65, 65, 65, 65, 61, 61
                });
    }

    private class TestAttachmentBuilder extends BaseAttachmentBuilder {

        public TestAttachmentBuilder(ContentModifier... modifiers) {
            initModifiers(TestUtils.map().withEntry(Constants.ATTACHMENT_CONTENT_MODIFIERS, modifiers).get());
        }

        @Override
        public XRoadAttachment buildAttachment(Map<String, Object> parameters) {
            return null;
        }

        @Override
        public AttachmentBuilder initBuilder(Map<String, Object> parameters) {
            return null;
        }

    }
}
