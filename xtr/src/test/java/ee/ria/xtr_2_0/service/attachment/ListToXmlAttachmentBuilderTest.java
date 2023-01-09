package ee.ria.xtr_2_0.service.attachment;

import com.nortal.jroad.model.XRoadAttachment;
import ee.ria.xtr_2_0.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ListToXmlAttachmentBuilderTest {

    @Test
    void testBuildAttachment() {
        String listProperty = "users";
        ListToXmlAttachmentBuilder builder = new ListToXmlAttachmentBuilder();

        String header = "<?xml version=\"1.0\" encoding=\"utf-8\"?><getUserInfoRequestAttachmentV1><user_list>";
        String element = "<code>%s</code>";
        String footer = "</user_list></getUserInfoRequestAttachmentV1>";

        ReflectionTestUtils.setField(builder, "header", header);
        ReflectionTestUtils.setField(builder, "element", element);
        ReflectionTestUtils.setField(builder, "footer", footer);
        ReflectionTestUtils.setField(builder, "listProperty", listProperty);

        Map<String, Object> parameters = TestUtils.
                map()
                .withEntry(listProperty, new String[] { "123", "456", "789" })
                .get();

        XRoadAttachment attachment = builder.buildAttachment(parameters);
        assertThat(attachment).isNotNull();
        assertThat(attachment.getCid()).isNotNull();
        assertThat(attachment.getContentType()).isEqualTo("text/xml");
    }

}
