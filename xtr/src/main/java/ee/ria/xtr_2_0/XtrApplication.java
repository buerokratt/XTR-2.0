package ee.ria.xtr_2_0;

import ee.ria.xtr_2_0.conf.XtrConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class XtrApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        new SpringApplicationBuilder(XtrConfiguration.class).run(args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(XtrApplication.class);
    }

}
