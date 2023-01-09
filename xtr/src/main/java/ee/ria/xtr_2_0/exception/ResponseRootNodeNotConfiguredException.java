package ee.ria.xtr_2_0.exception;

import ee.ria.xtr_2_0.model.XtrDatabase;

public class ResponseRootNodeNotConfiguredException extends XtrException {

    public ResponseRootNodeNotConfiguredException(XtrDatabase xtrDatabase) {
        put("registry.code", xtrDatabase.getRegistryCode());
        put("service.code", xtrDatabase.getServiceCode());
    }

}
