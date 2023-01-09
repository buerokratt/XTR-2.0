package ee.ria.xtr_2_0.service.attachment;

import com.nortal.jroad.model.XRoadAttachment;

import java.util.Map;

public interface AttachmentBuilder {

    /**
     *
     * @param parameters  attachment parameters
     * @return built attachment
     */
    XRoadAttachment buildAttachment(Map<String, Object> parameters);

    /**
     * Initializes the builder with necessary information from attachment parameters
     * @param parameters attachment parameters
     * @return AttachmentBuilder instance
     */
    AttachmentBuilder initBuilder(Map<String, Object> parameters);

}
