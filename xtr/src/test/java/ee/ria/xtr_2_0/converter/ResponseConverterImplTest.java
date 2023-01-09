package ee.ria.xtr_2_0.converter;

import com.google.common.collect.Maps;
import ee.ria.xtr_2_0.exception.AttachmentCreationException;
import ee.ria.xtr_2_0.helper.Constants;
import ee.ria.xtr_2_0.model.IntermediateConversionObject;
import ee.ria.xtr_2_0.model.XtrAttachment;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponseConverterImplTest {

    private final ResponseConverterImpl converter = new ResponseConverterImpl();

    @Test
    void convertingAttachmentThrowsExceptionIfRootElementIsNotConfigured() throws UnsupportedEncodingException {
        String attachmentString = "<foo></foo>";
        XtrAttachment attachmentConf = new XtrAttachment(Maps.newHashMap(), Maps.newHashMap());
        assertThatThrownBy(() -> converter.convertAttachment(
                new ByteArrayInputStream(attachmentString.getBytes(StandardCharsets.UTF_8)),
                attachmentConf
        )).isInstanceOf(AttachmentCreationException.class);
    }

    @Test
    @Disabled
    void testConvertAttachment() throws UnsupportedEncodingException {
        String attachmentString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><foo><bar>asd</bar><bar>qwer</bar></foo>";
        HashMap<String, Object> response = Maps.newHashMap();
        response.put(Constants.ATTACHMENT_ROOT_ELEMENT_NAME, "foo");

        XtrAttachment attachmentConf = new XtrAttachment(Maps.newHashMap(), response);
        IntermediateConversionObject result = converter.convertAttachment(
                new ByteArrayInputStream(attachmentString.getBytes(StandardCharsets.UTF_8)),
                attachmentConf
        );

        assertThat(result).isNotNull();
        assertThat(result.getObjectKey()).isEqualTo("foo");
        assertThat(result.getProperties().iterator().next().getCollection())
                .extracting(IntermediateConversionObject::getSingleValue)
                .containsExactlyInAnyOrder("asd", "qwer");
    }

}
