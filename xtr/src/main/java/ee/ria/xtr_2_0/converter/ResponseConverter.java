package ee.ria.xtr_2_0.converter;

import com.nortal.jroad.model.XRoadMessage;
import ee.ria.xtr_2_0.model.IntermediateConversionObject;
import ee.ria.xtr_2_0.model.XtrDatabase;
import org.apache.xmlbeans.XmlObject;

/**
 * Interface for converting X-Road responses
 */
public interface ResponseConverter {

    /**
     *
     * @param soapResponse from X-Road service
     * @param databaseConf local configuration related to that service
     * @return will be used when constructing XtrResponse returned by XtrController
     * @see ee.ria.xtr_2_0.model.XtrResponse
     * @see ee.ria.xtr_2_0.rest.controller.XtrController
     */
    IntermediateConversionObject convert(XmlObject soapResponse, XtrDatabase databaseConf);

    /**
     *
     * @param message from X-Road service
     * @param database local configuration related to that service
     * @return will be used when constructing XtrResponse returned by XtrController
     */
    IntermediateConversionObject convertAttachment(XRoadMessage<?> message, XtrDatabase database);

}
