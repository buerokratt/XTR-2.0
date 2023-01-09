package ee.ria.xtr_2_0.service;

import ee.ria.xtr_2_0.model.XtrRequest;
import ee.ria.xtr_2_0.model.XtrResponse;

/**
 * Interface for service(s) that would perform XtrRequests
 */
public interface XtrServiceWrapper {

    XtrResponse execute(XtrRequest req);

}
