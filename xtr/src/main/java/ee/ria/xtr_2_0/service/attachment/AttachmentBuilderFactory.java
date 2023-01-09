package ee.ria.xtr_2_0.service.attachment;

import ee.ria.xtr_2_0.model.XtrAttachment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

/**
 * A Factory for AttachmentBuilder instances
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AttachmentBuilderFactory {

    private static final String TYPE_PARAMETER = "type";

    /**
     *
     * @param attachmentInfo contains required info to build an attachment
     * @return optional is filled with AttachmentBuilder according to the type parameter in attachmentInfo request
     * @see XtrAttachment#request
     */
    public static Optional<AttachmentBuilder> build(XtrAttachment attachmentInfo) {
        return attachmentInfo != null ? findBuilder(attachmentInfo.getRequest()) : Optional.empty();
    }

    private static Optional<AttachmentBuilder> findBuilder(Map<String, Object> requestInfo) {
        return requestInfo != null && requestInfo.containsKey(TYPE_PARAMETER) ?
                findBuilder(String.valueOf(requestInfo.get(TYPE_PARAMETER)), requestInfo) : Optional.empty();
    }

    private static Optional<AttachmentBuilder> findBuilder(String name, Map<String, Object> requestInfo) {
        try {
            return Optional.of(AttachmentBuilderType.valueOf(name).newBuilder().initBuilder(requestInfo));
        }
        catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Possible AttachmentBuilder Types
     */
    public enum AttachmentBuilderType {

        LIST_TO_XML {
            @Override
            public AttachmentBuilder newBuilder() {
                return new ListToXmlAttachmentBuilder();
            }
        },

        JSON_TO_XML {
            @Override
            public AttachmentBuilder newBuilder() {
                return new JsonToXmlAttachmentBuilder();
            }
        };

        public abstract AttachmentBuilder newBuilder();

    }

}
