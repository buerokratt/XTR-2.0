package ee.ria.xtr_2_0.service.attachment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.nortal.jroad.model.XRoadAttachment;
import ee.ria.xtr_2_0.TestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JsonToXmlAttachmentBuilderTest {

    @Test
    void testBuildAttachment() throws IOException {
        JsonToXmlAttachmentBuilder builder = new JsonToXmlAttachmentBuilder();

        ReflectionTestUtils.setField(builder, "rootElement", "some_list");

        Map<String, Object> parameters = TestUtils.map()
                .withEntry("asd", new String[] { "123", "456", "789" })
                .withEntry("dsa", Lists.newArrayList("asd", "qwe"))
                .withEntry("foo", "foo-foo")
                .withEntry("bar", "true")
                .get();

        XRoadAttachment attachment = builder.buildAttachment(parameters);
        assertThat(attachment).isNotNull();
        assertThat(attachment.getCid()).isNotNull();
        assertThat(attachment.getContentType()).isEqualTo("text/xml");

        assertThat(IOUtils.toString(attachment.getDataHandler().getInputStream()))
                .contains("<some_list>")
                .contains("<asd>123</asd>")
                .contains("<asd>456</asd>")
                .contains("<asd>789</asd>")
                .contains("<dsa>asd</dsa>")
                .contains("<dsa>qwe</dsa>")
                .contains("<foo>foo-foo</foo>")
                .contains("<bar>true</bar>")
                .contains("</some_list>");
    }

    @Test
    void testBuildNestedAttachment() throws IOException {
        JsonToXmlAttachmentBuilder builder = new JsonToXmlAttachmentBuilder();

        ReflectionTestUtils.setField(builder, "rootElement", "yo-yo");
        ReflectionTestUtils.setField(builder, "nestedArrays", TestUtils.map().withEntry("files", "the_file").get());

        Map<String, Object> parameters = TestUtils.map()
                .withEntry(
                        "document", TestUtils.map()
                                .withEntry("foo", "asd")
                                .withEntry(
                                        "files", new Map[] {
                                                TestUtils.map().withEntry("qqq", "qwe").get(),
                                                TestUtils.map().withEntry("qqq", "www").get()
                                        }
                                ).get()
                ).get();

        XRoadAttachment attachment = builder.buildAttachment(parameters);

        String content = IOUtils.toString(attachment.getDataHandler().getInputStream());

        System.out.println(content);

        assertThat(content)
                .contains("<yo-yo>")
                .contains("<document>")
                .contains("<foo>asd</foo>")
                .contains("<files>")
                .contains("<the_file>")
                .contains("<qqq>qwe</qqq>")
                .contains("<qqq>www</qqq>")
                .doesNotContain("<files/>")
                .contains("</document>")
                .contains("</yo-yo>");
    }

    @Test
    void testIgnoreParameters() throws IOException {
        JsonToXmlAttachmentBuilder builder = new JsonToXmlAttachmentBuilder();

        ReflectionTestUtils.setField(builder, "rootElement", "some_list");
        ReflectionTestUtils.setField(builder, "ignoredFields", Sets.newHashSet("foo"));

        Map<String, Object> parameters = TestUtils.map()
                .withEntry("foo", "bar")
                .withEntry("asd", "true")
                .get();

        XRoadAttachment attachment = builder.buildAttachment(parameters);
        assertThat(attachment).isNotNull();
        assertThat(attachment.getCid()).isNotNull();
        assertThat(attachment.getContentType()).isEqualTo("text/xml");

        assertThat(IOUtils.toString(attachment.getDataHandler().getInputStream()))
                .contains("<asd>true</asd>")
                .doesNotContain("foo")
                .doesNotContain("bar");
    }

}
