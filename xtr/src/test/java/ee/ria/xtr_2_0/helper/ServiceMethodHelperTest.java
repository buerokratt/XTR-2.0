package ee.ria.xtr_2_0.helper;

import com.google.common.collect.Maps;
import com.nortal.jroad.model.XRoadMessage;
import ee.ria.xtr_2_0.model.ServiceMethodWithType;
import ee.ria.xtr_2_0.model.XtrAttachment;
import ee.ria.xtr_2_0.model.XtrDatabase;
import org.apache.xmlbeans.impl.values.XmlBooleanImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceMethodHelperTest {

    private final ServiceBean serviceBean = new ServiceBean();


    @Test
    void requestWithoutAttachmentReturnsActualMethod() {
        String method = "methodFoo";
        XtrDatabase db = new XtrDatabase();
        db.setMethod(method);

        ServiceMethodWithType result = ServiceMethodHelper.findMethod(serviceBean, db);
        assertThat(result.getMethod().getName()).isEqualTo(method);
        assertThat(result.getArgumentType()).isEqualTo(XmlBooleanImpl.class);
    }

    @Test
    void requestWithAttachmentReturnsSendMethod() {
        String method = "methodFoo";
        XtrDatabase db = new XtrDatabase();
        db.setMethod(method);
        db.setAttachments(new XtrAttachment(Maps.newHashMap(), null));

        ServiceMethodWithType result = ServiceMethodHelper.findMethod(serviceBean, db);
        assertThat(result.getMethod().getName()).isEqualTo(Constants.METHOD_SEND);
        assertThat(result.getArgumentType()).isEqualTo(XmlBooleanImpl.class);
    }

    private abstract static class BaseServiceBean {

        protected <I, O> XRoadMessage<O> send(XRoadMessage<I> input, String method,
                                              String version, final String idCode) {
            return null;
        }

    }

    private static class ServiceBean extends BaseServiceBean {

        public String methodFoo(XmlBooleanImpl arg, String idCode) {
            return null;
        }

    }

}
