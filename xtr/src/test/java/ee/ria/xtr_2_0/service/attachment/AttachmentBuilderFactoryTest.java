package ee.ria.xtr_2_0.service.attachment;

import ee.ria.xtr_2_0.TestUtils;
import ee.ria.xtr_2_0.model.XtrAttachment;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AttachmentBuilderFactoryTest {

    @Test
    void testBuildListToXmlAttachmentBuilder() {
        XtrAttachment attachments = new XtrAttachment();
        String header = "<body>";
        String element = "<element/>";
        String footer = "</body>";
        String listProperty = "code";

        attachments.setRequest(
                TestUtils.map()
                        .withEntry("type", "LIST_TO_XML")
                        .withEntry("header", header)
                        .withEntry("element", element)
                        .withEntry("footer", footer)
                        .withEntry("listProperty", listProperty)
                        .get()
        );

        Optional<AttachmentBuilder> optional = AttachmentBuilderFactory.build(attachments);
        assertThat(optional).isNotEmpty();

        AttachmentBuilder builder = optional.get();
        assertThat(builder).isInstanceOf(ListToXmlAttachmentBuilder.class);

        ListToXmlAttachmentBuilder xmlBuilder = (ListToXmlAttachmentBuilder) builder;
        assertThat(stringValue(xmlBuilder, "header")).isEqualTo(header);
        assertThat(stringValue(xmlBuilder, "element")).isEqualTo(element);
        assertThat(stringValue(xmlBuilder, "footer")).isEqualTo(footer);
        assertThat(stringValue(xmlBuilder, "listProperty")).isEqualTo(listProperty);
    }

    private static String stringValue(ListToXmlAttachmentBuilder xmlBuilder, String key) {
        return String.valueOf(ReflectionTestUtils.getField(xmlBuilder, key));
    }


}
