package ee.ria.xtr_2_0.service.attachment;

import ee.ria.xtr_2_0.exception.AttachmentCreationException;
import ee.ria.xtr_2_0.helper.Constants;
import ee.ria.xtr_2_0.helper.ObjectStreamHelper;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Abstract superclass
 */
public abstract class BaseAttachmentBuilder implements AttachmentBuilder {

    protected ContentModifier[] modifiers;

    /**
     * Filters out attachment content modifiers from and sets them as a protected field.
     * These modifiers are used to modify attachment content before building.
     * @param parameters attachment parameters
     */
    protected void initModifiers(Map<String, Object> parameters) {
        if (parameters.containsKey(Constants.ATTACHMENT_CONTENT_MODIFIERS)) {
            modifiers = ObjectStreamHelper.stream(parameters.get(Constants.ATTACHMENT_CONTENT_MODIFIERS))
                    .map(String::valueOf)
                    .map(ContentModifier::valueOf)
                    .toArray(ContentModifier[]::new);
        }
    }

    /**
     * If any content modifiers were set for this builder then the byte array was further modified before returning it
     * @param content input content
     * @return the content representation as byte array,
     */
    protected byte[] getBytes(String content) {
        ContentModifierHelper cmh = new ContentModifierHelper(content);

        if (modifiers != null) {
            Arrays.stream(modifiers)
                    .sorted(Comparator.comparingInt(ContentModifier::getOrder).reversed())
                    .forEach(cmh::modify);
        }

        return cmh.getContent();
    }


    private static class ContentModifierHelper {

        @Getter
        private byte[] content;

        public ContentModifierHelper(String content) {
            try {
                this.content = content.getBytes(Constants.CHARSET_UTF_8);
            }
            catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public void modify(ContentModifier modifier) {
            content = modifier.modify(content);
        }

    }

    public enum ContentModifier {

        GZIP(Integer.MAX_VALUE) {
            @Override
            public byte[] modify(byte[] bytes) {
                if (bytes == null) {
                    bytes = new byte[0];
                }

                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                     GZIPOutputStream outputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                    outputStream.write(bytes);
                    outputStream.finish();
                    outputStream.flush();
                    return byteArrayOutputStream.toByteArray();
                }
                catch (Exception e) {
                    throw new AttachmentCreationException("Can't compress attachment", e);
                }
            }
        },

        BASE64(1) {
            @Override
            public byte[] modify(byte[] bytes) {
                return Base64.getEncoder().encode(bytes);
            }
        };

        @Getter
        public int order;

        public abstract byte[] modify(byte[] bytes);

        ContentModifier(int order) {
            this.order = order;
        }
    }

}
