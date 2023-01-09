package ee.ria.xtr_2_0.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import ee.ria.xtr_2_0.TestUtils;
import ee.ria.xtr_2_0.converter.RequestConverter;
import ee.ria.xtr_2_0.converter.ResponseConverter;
import ee.ria.xtr_2_0.model.ConvertedRequest;
import ee.ria.xtr_2_0.model.XtrDatabase;
import ee.ria.xtr_2_0.model.XtrRequest;
import lombok.AllArgsConstructor;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.NamespaceManager;
import org.apache.xmlbeans.impl.values.XmlAnySimpleTypeImpl;
import org.apache.xmlbeans.impl.values.XmlObjectBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XtrServiceWrapperImplTest {

    private static final String REGISTRY_CODE = "reg";
    private static final String SERVICE_CODE = "ser";
    private static final String SERVICE_NAME = "serviceBean";
    private static final String SERVICE_METHOD = "doService";

    private static final String PERSONAL_CODE = "callerPersonalCode";


    @Mock
    private ApplicationContext ctx;

    @Mock
    private XtrDatabaseConfLoader confLoader;

    @Mock
    private ResponseConverter responseConverter;

    @Mock
    private RequestConverter requestConverter;

    @InjectMocks
    private XtrServiceWrapperImpl wrapper;

    private DummyServiceBean serviceBean;

    @BeforeEach
    void setUp() {
        wrapper.setApplicationContext(ctx);
        ReflectionTestUtils.setField(wrapper, "callerIdCodeKeys", Lists.newArrayList(PERSONAL_CODE));
        ReflectionTestUtils.setField(wrapper, "xtrDatabases", Sets.newHashSet(new XtrDatabase(
                REGISTRY_CODE, SERVICE_CODE, SERVICE_NAME, "blah", "1", SERVICE_METHOD, "node", "http://foo.com", null
        )));

        serviceBean = new DummyServiceBean();
        when(ctx.getBean(SERVICE_NAME)).thenReturn(serviceBean);
    }

    @Test
    void ifMultipleInputsThenMakeMultipleXteeRequests() {
        String multiInputField = "someInteger";
        Integer[] multiInputValue = new Integer[]{1, 2, 5, 7};

        Map<String, Object> params = TestUtils.map()
                .withEntry(multiInputField, multiInputValue)
                .withEntry(PERSONAL_CODE, "38001010101")
                .get();

        when(requestConverter.convert(eq(ServiceRequest.class), anyMap(), isNull())).thenAnswer(inv -> {
            Map<String, Object> parameters = inv.getArgument(1);
            return new ConvertedRequest<>(new ServiceRequest(
                    (Integer) parameters.get(multiInputField),
                    (String) parameters.get(PERSONAL_CODE)
            ));
        });

        XtrRequest req = new XtrRequest(REGISTRY_CODE, SERVICE_CODE, null, params, multiInputField, null);

        wrapper.execute(req);

        assertThat(serviceBean.data.keySet()).containsExactlyInAnyOrder(multiInputValue);
    }

    private class DummyServiceBean {

        private Map<Integer, String> data = Maps.newHashMap();

        public ServiceResponse doService(ServiceRequest param, String personalCode) {
            data.put(param.someInteger, personalCode);
            return new ServiceResponse(param.someInteger, personalCode);
        }

    }

    @AllArgsConstructor
    private static class ServiceRequest extends XmlObjectBase {

        private Integer someInteger;
        private String personalCode;

        @Override
        public SchemaType schemaType() {
            return null;
        }

        @Override
        protected void set_text(String s) {

        }

        @Override
        protected void set_nil() {

        }

        @Override
        protected String compute_text(NamespaceManager namespaceManager) {
            return null;
        }

        @Override
        protected boolean equal_to(XmlObject xmlObject) {
            return false;
        }

        @Override
        protected int value_hash_code() {
            return 0;
        }
    }

    @AllArgsConstructor
    private class ServiceResponse extends XmlAnySimpleTypeImpl {

        private Integer someInteger;
        private String personalCode;

    }

}
