package ee.ria.xtr_2_0.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data class that  contains details about X-Road service method.
 * Provides mapping of XtrRequest register (registryCode) and service (serviceCode)
 * to a service bean's name (serviceName) and that beans method (method).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class XtrDatabase {

    /**
     * Corresponds to XtrRequest.register
     */
    private String registryCode;

    /**
     * Corresponds to XtrRequest.service
     */
    private String serviceCode;

    /**
     * service beans name
     */
    private String serviceName;

    private String operationName;

    private String version;

    /**
     * method name in service bean
     */
    private String method;

    /**
     * defines what would be the root node inside response returned by X-Road service
     */
    private String responseRootNode;

    private String namespaceUri;

    private XtrAttachment attachments;

    public boolean isAttachmentNotEmpty() {
        return attachments != null && (attachments.getRequest() != null || attachments.getResponse() != null);
    }

}
