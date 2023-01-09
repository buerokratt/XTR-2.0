package ee.ria.xtr_2_0.service;

import com.google.common.collect.Maps;
import com.nortal.jroad.client.service.BaseXRoadDatabaseService;
import ee.ria.xtr_2_0.model.XtrDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XtrDatabaseConfLoaderImplTest {

    @Mock
    private ApplicationContext ctx;

    @InjectMocks
    private XtrDatabaseConfLoaderImpl confLoader;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(confLoader, "xteeServicesPath",
                getClass().getClassLoader().getResource("db_conf").getFile());
        confLoader.setApplicationContext(ctx);
    }

    @Test
    void testLoadConf() {
        String aar = "aarXRoadDatabase";
        String arireg = "ariregXRoadDatabase";

        Map<String, BaseXRoadDatabaseService> databases = Maps.newHashMap();
        databases.put(aar, null);
        databases.put(arireg, null);
        when(ctx.getBeansOfType(BaseXRoadDatabaseService.class)).thenReturn(databases);

        Set<XtrDatabase> xtrDatabases = confLoader.loadConf();
        assertThat(xtrDatabases).hasSize(2);
        for (XtrDatabase db : xtrDatabases) {
            if (aar.equals(db.getServiceName())) {
                assertThat(db.getAttachments()).isNotNull();
                Map<String, Object> requestData = db.getAttachments().getRequest();
                assertThat(requestData).isNotNull();
                assertThat(requestData).containsKeys("type", "header");

            }
        }
    }

}
