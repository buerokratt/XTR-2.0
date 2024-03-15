package ee.ria.xtr_2_0.service;

import ee.ria.xtr_2_0.converter.RequestConverter;
import ee.ria.xtr_2_0.converter.ResponseConverter;
import ee.ria.xtr_2_0.exception.CallerIdMissingException;
import ee.ria.xtr_2_0.exception.DatabaseNotFoundException;
import ee.ria.xtr_2_0.exception.DatabaseServiceBeanNotFoundException;
import ee.ria.xtr_2_0.model.XtrDatabase;
import ee.ria.xtr_2_0.model.XtrRequest;
import ee.ria.xtr_2_0.model.XtrResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service that performs  XtrRequests
 */
@Service
@Slf4j
public class XtrServiceWrapperImpl implements XtrServiceWrapper, IdCodeLookup, ApplicationContextAware {

    private ApplicationContext ctx;

    private final Set<XtrDatabase> xtrDatabases;

    private final ResponseConverter responseConverter;

    private final RequestConverter requestConverter;

    @Value("${xtr.req.caller.idcode.key}")
    private Collection<String> callerIdCodeKeys;

    @Autowired
    public XtrServiceWrapperImpl(XtrDatabaseConfLoader confLoader, ResponseConverter responseConverter,
                                 RequestConverter requestConverter) {
        this.xtrDatabases = confLoader.loadConf();
        this.responseConverter = responseConverter;
        this.requestConverter = requestConverter;

        log.info("XTR registered databases:" +
                xtrDatabases.stream().map(db -> "[" + db.getRegistryCode() + ":" + db.getServiceCode() + "]")
                        .collect(Collectors.joining(",")));
    }

    /**
     * Performs the request and produces the response.
     * @param req the request to be performed
     * @return actual response depends on the input
     */
    @Override
    public XtrResponse execute(XtrRequest req) {
        final XtrDatabase db = findDatabase(req.getRegister(), req.getService());

        Object bean;
        try {
            // get bean and redirect request to that bean
            bean = ctx.getBean(db.getServiceName());
        }
        catch (BeansException e) {
            throw new DatabaseServiceBeanNotFoundException(db.getServiceName(), e);
        }

        // convert SOAP response to JSON
        return new MethodExecutorFactory(req, db).executor().execute(bean, requestConverter, this, responseConverter);
    }

    /**
     * Returns first entry from parameters the key of which matches any of the strings defined in property
     * <i>xtr.req.caller.idcode.key</i>
     * @param parameters map of request parameters
     * @return Caller Id code
     */
    @Override
    public String findIdCode(Map<String, Object> parameters) {
        String idCode = parameters.entrySet().stream()
                .filter(e -> callerIdCodeKeys.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .map(String::valueOf)
                .findFirst().orElseThrow(() -> new CallerIdMissingException(callerIdCodeKeys.toArray(new String[0])));

        log.debug("Caller ID code: {}", idCode);
        return idCode;
    }

    private XtrDatabase findDatabase(String register, String service) {
        XtrDatabase result = xtrDatabases.stream()
                .filter(db -> register.equals(db.getRegistryCode()) && service.equals(db.getServiceCode()))
                .findFirst().orElseThrow(() -> new DatabaseNotFoundException(register, service));
        log.debug("Database: {}", result);
        return result;
    }

    /**
     * Sets new application context
     * @param ctx to be loaded
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }

}
