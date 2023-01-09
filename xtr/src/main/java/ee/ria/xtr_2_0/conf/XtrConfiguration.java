package ee.ria.xtr_2_0.conf;

import com.nortal.jroad.client.service.configuration.provider.PropertiesBasedXRoadServiceConfigurationProvider;
import com.nortal.jroad.client.service.configuration.provider.XRoadServiceConfigurationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import java.util.Properties;

/**
 * Configuration class provides beans from annotated components as defined by @ComponentScan annotation.
 * Also provides beans as defined by public methods annotated with @Bean
 *
 */
@Configuration
@ComponentScan({
        "ee.ria.xtr_2_0",
        "com.nortal.jroad.client"
})
@PropertySource("${xroad.config.src:classpath:xroad.yaml}")
@RequiredArgsConstructor
@Slf4j
public class XtrConfiguration {

    private final Environment environment;

    /**
     *
     * @return XRoadServiceConfigurationProvider implementation
     */
    @Bean
    public XRoadServiceConfigurationProvider configurationProvider() {
        return new PropertiesBasedXRoadServiceConfigurationProvider() {

            @Override
            protected String getProperty(String target, String key) {
                String property = environment.getProperty(key);
                log.info("Found target {}, key {} : {}", target, key, property);
                return property;
            }

            @Override
            protected Properties loadProperties(Resource resource) {
                return null;
            }

        };
    }

}
