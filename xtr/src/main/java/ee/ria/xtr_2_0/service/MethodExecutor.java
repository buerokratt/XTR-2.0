package ee.ria.xtr_2_0.service;

import com.google.common.collect.Lists;
import com.nortal.jroad.model.XmlBeansXRoadMessage;
import ee.ria.xtr_2_0.converter.RequestConverter;
import ee.ria.xtr_2_0.converter.ResponseConverter;
import ee.ria.xtr_2_0.exception.MethodInvocationException;
import ee.ria.xtr_2_0.exception.ResponseCreationException;
import ee.ria.xtr_2_0.helper.ServiceMethodHelper;
import ee.ria.xtr_2_0.model.ConvertedRequest;
import ee.ria.xtr_2_0.model.IntermediateConversionObject;
import ee.ria.xtr_2_0.model.ServiceMethodWithType;
import ee.ria.xtr_2_0.model.XtrDatabase;
import ee.ria.xtr_2_0.model.XtrGroupedResponse;
import ee.ria.xtr_2_0.model.XtrRequest;
import ee.ria.xtr_2_0.model.XtrResponse;
import ee.ria.xtr_2_0.model.XtrSingleResponse;
import ee.ria.xtr_2_0.model.XtrUngroupedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.XmlObject;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Contains needed data and methods to perform the request specified in XtrRequest
 */
@Slf4j
public class MethodExecutor {

    /**
     * incoming request that needs execution
     */
    private final XtrRequest request;

    /**
     * contains xtr service details needed for execution
     */
    private final XtrDatabase database;

    /**
     * calls execution and produces response from invocation result
     */
    private final XtrResponseFactory responseFactory;

    /**
     * list of parameters for the execution. Used as input for function in
     * responsefactory.getResponse(Function<Map<String, Object>, IntermediateConversionObject> worker)
     */
    private final List<Map<String, Object>> parameters = Lists.newArrayList();

    /**
     * Constructs new MethodExecutor.
     * The responseFactory of the MethodExecutor will be one that provides single response or multiple ungrouped or
     * grouped responses as defined by multipleInputsFrom and groupResponseByField fields in request
     *
     * @param request request being handled
     * @param database database being used

     */
    public MethodExecutor(XtrRequest request, XtrDatabase database) {
        this.request = request;
        this.database = database;
        if (StringUtils.isEmpty(request.getMultipleInputsFrom())) {
            responseFactory = new SingleResponseFactory();
        }
        else if (StringUtils.isEmpty(request.getGroupResponseByField())) {
            responseFactory = new UngroupedResponseFactory();
        }
        else {
            responseFactory = new GroupedResponseFactory(request.getGroupResponseByField());
        }

        log.info("Method executor of type {}", responseFactory);
    }

    /**
     * Produces an XtrResponse. The request to be executed is aquired from this MethodExecutor field
     * "request" and parameters for execution from field "parameters".
     *
     * @param bean service bean, used to find service method from xtr database
     * @param requestConverter used to covert request to suitable format for service method
     * @param idCodeLookup  used to find caller id code
     * @param responseConverter used to convert response to suitable format
     * @return actual response content depends on input
     */
    public XtrResponse execute(Object bean, RequestConverter requestConverter,
                               IdCodeLookup idCodeLookup, ResponseConverter responseConverter) {
        // find actual method that does all the work
        ServiceMethodWithType methodWitType = ServiceMethodHelper.findMethod(bean, database);
        Method method = methodWitType.getMethod();
        // figure out method parameter type
        Class<XmlObject> methodParameterType = methodWitType.getArgumentType();
        // TODO: needs refactoring, it's written on sunday night
        return responseFactory.getResponse(params -> {
            // here the parameter is actually being built
            ConvertedRequest<?> parameter = requestConverter.convert(methodParameterType,
                    params, database.getAttachments());
            try {
                // and here  we make a call to an actual x-tee service method
                Object result;
                if (database.isAttachmentNotEmpty()) {
                    boolean accessible = method.canAccess(bean);
                    method.setAccessible(true);
                    XmlBeansXRoadMessage<?> message = parameter.getAttachment() == null
                            ? new XmlBeansXRoadMessage<>(parameter.getRequest())
                            : new XmlBeansXRoadMessage<>(
                            parameter.getRequest(), Lists.newArrayList(parameter.getAttachment())
                    );

                    result = method.invoke(bean, message, database.getOperationName(), database.getVersion(),
                            idCodeLookup.findIdCode(params));
                    method.setAccessible(accessible);
                }
                else {
                    result = method.invoke(bean, parameter.getRequest(), idCodeLookup.findIdCode(params));
                }

                if (result instanceof XmlObject) {
                    return responseConverter.convert((XmlObject) result, database);
                }
                else if (result instanceof XmlBeansXRoadMessage) {
                    // serialize attachment
                    XmlBeansXRoadMessage xRoadMessage = (XmlBeansXRoadMessage) result;
                    IntermediateConversionObject attachment =
                            responseConverter.convertAttachment(xRoadMessage, database);

                    // serialize response body
                    IntermediateConversionObject ico = responseConverter.convert(xRoadMessage.getContent(), database);
                    // add attachment to JSON response
                    ico.addProperty(attachment);
                    return ico;
                }
                else {
                    throw new ResponseCreationException("Invalid response type: " + result.getClass().getName());
                }

            }
            catch (Throwable t) {
                log.error("Error while performing request {}", t.getCause().getMessage(), t);
                throw new MethodInvocationException(method, t);
            }
        });
    }

    /**
     * Constructs and returns an instance of the nested class MethodExecutor.Factory which has
     * new instance of MethodExecutor(request,database) as a  final field
     *
     * @param request request at hand
     * @param database database used
     * @return has an instance of MethodExecutor constructed with request and datbase parameters
     */
    public static Factory factory(XtrRequest request, XtrDatabase database) {
        return new Factory(request, database);
    }

    /*
     *  METHOD EXECUTOR FACTORY
     */

    /**
     * Nested factory class
     */
    public static class Factory {

        private final MethodExecutor executor;

        private Factory(XtrRequest request, XtrDatabase database) {
            executor = new MethodExecutor(request, database);
        }

        /**
         *
         * @param parameters list of parameters for the executor
         * @return the same Factory instance but the executor that can be returned by build method now has parameters attached
         */
        public Factory withParams(Map<String, Object> parameters) {
            executor.parameters.add(parameters);
            return this;
        }

        /**
         * Executor that is returned was constructed at the same time as this Factory with the same "request" and "database" constructor parameters.
         * It may or may not have executor.parameters parameter list,  depending if MethodExecutor.Factory.withParams has been called
         * @return executor to be returned
         */
        public MethodExecutor build() {
            return executor;
        }

    }

    /*
     *  RESPONSE FACTORIES
     */

    private interface XtrResponseFactory {

        XtrResponse getResponse(Function<Map<String, Object>, IntermediateConversionObject> worker);

    }

    private class SingleResponseFactory implements XtrResponseFactory {

        @Override
        public XtrResponse getResponse(Function<Map<String, Object>, IntermediateConversionObject> worker) {
            log.debug("Request {}, parameters {}", request, parameters);
            return new XtrSingleResponse(request, worker.apply(parameters.get(0)));
        }

    }

    private class UngroupedResponseFactory implements XtrResponseFactory {

        @Override
        public XtrResponse getResponse(Function<Map<String, Object>, IntermediateConversionObject> worker) {
            XtrUngroupedResponse response = new XtrUngroupedResponse(request);
            parameters.forEach(p -> response.getResponse().add(worker.apply(p)));
            return response;
        }

    }

    @RequiredArgsConstructor
    private class GroupedResponseFactory implements XtrResponseFactory {

        private final String groupBy;

        @Override
        public XtrResponse getResponse(Function<Map<String, Object>, IntermediateConversionObject> worker) {
            XtrGroupedResponse response = new XtrGroupedResponse(request);
            parameters.forEach(p -> response.getResponse().put(p.get(groupBy), worker.apply(p)));
            return response;
        }

    }


}
