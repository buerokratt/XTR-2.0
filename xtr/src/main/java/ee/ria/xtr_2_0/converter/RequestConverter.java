package ee.ria.xtr_2_0.converter;

import ee.ria.xtr_2_0.model.ConvertedRequest;
import ee.ria.xtr_2_0.model.XtrAttachment;
import org.apache.xmlbeans.XmlObject;

public interface RequestConverter {

    /**
     *
     * For producing ConvertedRequest that is then used when performing actual X-Road request
     * @param type parameter type must implement XmlObject
     * @param data mapped request parameters
     * @param attachmentInfo information about attachments
     * @return conversion result
     */
    <T extends XmlObject> ConvertedRequest<T> convert(Class<T> type, Object data, XtrAttachment attachmentInfo);

}
