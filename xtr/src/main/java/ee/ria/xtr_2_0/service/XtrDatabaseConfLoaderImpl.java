package ee.ria.xtr_2_0.service;

import com.nortal.jroad.client.service.BaseXRoadDatabaseService;
import com.nortal.jroad.client.service.XRoadDatabaseService;
import ee.ria.xtr_2_0.model.XtrDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loads XtrDatabase configurations from yaml files
 */
@Component
@Slf4j
public class XtrDatabaseConfLoaderImpl implements XtrDatabaseConfLoader, ApplicationContextAware {
    private static final String YAML_EXTENSION = ".yaml";

    @Value("${xtr.xtee.services.path:services}")
    private String xteeServicesPath;

    private ApplicationContext ctx;

    @Override
    public Set<XtrDatabase> loadConf() {
        File servicesPath = new File(xteeServicesPath);

        log.info("Initializing services from {}", servicesPath.getAbsolutePath());

        if (!servicesPath.exists() || !servicesPath.isDirectory()) {
            throw new IllegalArgumentException(servicesPath.getAbsolutePath()
                    + " does not exist or is not a directory");
        }

        final Yaml yaml = new Yaml();
        Set<String> xteeServiceBeans = ctx.getBeansOfType(BaseXRoadDatabaseService.class).keySet();
        //log.info("all service beans: "+String.join(",",ctx.getBeanDefinitionNames()));

        log.info("xtee service beans: "+xteeServiceBeans.stream().collect(Collectors.joining(", ")));

        xteeServiceBeans.forEach(bean -> log.debug("Found X-Tee service bean: {}", bean));

        Set<XtrDatabase> result = Arrays.stream(servicesPath.listFiles((dir, name) -> name.endsWith(YAML_EXTENSION)))
                .map(file -> {
                    log.debug("Processing file: {}", file.getName());
                    InputStream in = null;
                    try {
                        in = new FileInputStream(file);
                        XtrDatabase database = yaml.loadAs(in, XtrDatabase.class);
                        log.debug("Created: {}", database);
                        return database;
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    finally {
                        if (in != null) {
                            try {
                                in.close();
                            }
                            catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                })
                .filter(db -> xteeServiceBeans.contains(db.getServiceName()))
                .collect(Collectors.toSet());

        StringBuilder sb = new StringBuilder();
        String sbs = result.stream().sorted(
                Comparator.comparing(XtrDatabase::getRegistryCode).thenComparing(XtrDatabase::getServiceCode)
        ).map(db -> "\t" + db + "\n").collect(Collectors.joining());

        log.info("X-Tee services:\n\n{}", sbs.toString());

        return result;
    }

    /**
     * Sets new context
     * @param ctx context to be loaded
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }
}
