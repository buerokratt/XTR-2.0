package ee.ria.xtr_2_0.converter;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.nortal.jroad.model.XRoadAttachment;
import ee.ria.xtr_2_0.converter.utils.CalendarConverter;
import ee.ria.xtr_2_0.exception.ArgumentCreationException;
import ee.ria.xtr_2_0.exception.ParameterFactoryNotFoundException;
import ee.ria.xtr_2_0.helper.Constants;
import ee.ria.xtr_2_0.model.ConvertedRequest;
import ee.ria.xtr_2_0.model.XtrAttachment;
import ee.ria.xtr_2_0.service.attachment.AttachmentBuilderFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.apache.xmlbeans.StringEnumAbstractBase;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RequestConverterImpl implements RequestConverter {

    @Value("${xtr.req.caller.idcode.key}")
    private Collection<String> callerIdCodeKeys;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends XmlObject> ConvertedRequest<T> convert(Class<T> type, Object data, XtrAttachment attachmentInfo) {
        try {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            XRoadAttachment attachment = AttachmentBuilderFactory.build(attachmentInfo)
                    .map(ab -> ab.buildAttachment(dataMap)).orElse(null);

            Object request = XmlString.class.isAssignableFrom(type) ?
                    createString(filterOutIdCode(dataMap)) : createUnchecked(type, dataMap, attachment);
            return new ConvertedRequest(type.cast(request), attachment);
        }
        catch (Exception e) {
            throw new ArgumentCreationException(e.getCause());
        }
    }

    private XmlString createString(Map<String, Object> dataMap) {
        if (dataMap.size() != 1) {
            throw new ArgumentCreationException("Unable to create XmlString element. " +
                    "Required exactly 1 argument but got " + dataMap.size());
        }

        XmlString result = XmlString.Factory.newInstance();
        result.setStringValue(String.valueOf(dataMap.values().iterator().next()));
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Object createUnchecked(Class<?> type, Object data, XRoadAttachment attachment) throws Exception {
        log.debug("Creating parameter of type [{}]", type);
        Object result = null;
        if (XmlObject.class.isAssignableFrom(type)) {
            // find factory and create new
            result = Arrays.stream(type.getClasses()).filter(c -> c.getSimpleName().equals(Constants.CLASS_FACTORY))
                    .map(c -> {
                        try {
                            return c.getMethod(Constants.METHOD_NEW_INSTANCE);
                        }
                        catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .findFirst().orElseThrow(() -> new ParameterFactoryNotFoundException(type))
                    .invoke(null);
            if (data instanceof List) {
                processListElements((List<Object>) data, attachment, result);
            } else {
                XmlElementProcessorFactory.map((Map<String, Object>) data, attachment).processElement(result);
            }
        }
        else if (ConvertUtils.lookup(data.getClass(), type) != null) {
            if (Calendar.class.equals(type)) {
                ConvertUtils.register(new CalendarConverter(), Calendar.class);
            }

            result = ConvertUtils.convert(data, type);
        }
        else if (StringEnumAbstractBase.class.isAssignableFrom(type)) {
            try {
                Method forString = type.getMethod(Constants.METHOD_ENUM_FOR_STRING, String.class);
                return forString.invoke(null, String.valueOf(data));
            }
            catch (NoSuchMethodException e) {
                throw new ParameterFactoryNotFoundException(type);
            }
            catch (Exception e) {
                throw new ArgumentCreationException(e);
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static void processListElements(List<Object> data, XRoadAttachment attachment, Object result){
        for (Object object : data) {
            XmlElementProcessorFactory.map((Map<String, Object>) object, attachment).processElement(result);
        }
    }

    private Map<String, Object> filterOutIdCode(Map<String, Object> parameters) {
        Map<String, Object> result = Maps.newHashMap(parameters);
        callerIdCodeKeys.forEach(result::remove);
        return result;
    }

    /*
     *  XML ELEMENT PROCESSORS
     */

    @RequiredArgsConstructor
    private static abstract class XmlElementProcessor<DATA_TYPE> {

        protected final DATA_TYPE data;

        protected final XRoadAttachment attachment;

        public abstract void processElement(Object element);

        protected void checkAddNew(Object element) {
            // for overriding
        }

        protected void checkSetters(Object element) {
            // for overriding
        }

    }

    /*
     *  XML ELEMENT PROCESSOR: MAP PARAMETERS
     */

    private static class XmlElementProcessorMap extends XmlElementProcessor<Map<String, Object>> {

        private final Set<String> addNewElementNames = Sets.newHashSet();

        private XmlElementProcessorMap(Map<String, Object> data, XRoadAttachment attachment) {
            super(data, attachment);
        }

        @Override
        public void processElement(Object element) {
            checkAddNew(element);
            checkSetters(element);
            if (attachment != null) {
                checkHref(element);
            }
        }

        @Override
        protected void checkAddNew(Object element) {
            Method[] methods = element.getClass().getMethods();
            Arrays.stream(methods)
                    .filter(m -> m.getName().startsWith(Constants.METHOD_ADD_NEW))
                    .forEach(m -> {
                        String methodName = m.getName();
                        String elementName = methodName.substring(Constants.METHOD_ADD_NEW.length());
                        if (Arrays.stream(methods)
                                .anyMatch(method -> method.getName().equals(Constants.METHOD_REMOVE + elementName))) {
                            return;
                        }
                        addNewElementNames.add(elementName);
                        // alright, here's an addNew method.
                        log.debug("Found add new method [{}.{}]. Return type [{}]",
                                element.getClass().getSimpleName(), methodName, m.getReturnType());
                        try {
                            Object o = data.get(elementName);
                            if (o == null || o instanceof Map) {
                                // it's a body element
                                XmlElementProcessorFactory.convertedMap(data, attachment)
                                        .processElement(m.invoke(element));
                            }
                            else if (o.getClass().isArray()) {
                                XmlElementProcessorFactory.array(convertToObjectArray(o), attachment)
                                        .processElement(m.invoke(element));
                            }
                        }
                        catch (Exception e) {
                            throw new ArgumentCreationException(e);
                        }
                    });
        }

        @Override
        protected void checkSetters(Object element) {
            Arrays.stream(element.getClass().getMethods())
                    .filter(m -> {
                        String paramName = CaseUtils.toCamelCase(m.getName().substring(Constants.METHOD_SETTER_PREFIX.length()), true, '_');
                        return data.containsKey(paramName)
                                && m.getParameterCount() == 1
                                && !addNewElementNames.contains(paramName);
                    })
                    .forEach(m -> {
                        String methodName = m.getName();
                        Class<?> parameterType = m.getParameters()[0].getType();
                        log.debug("Found setter method [{}.{}]. Parameter type [{}]",
                                element.getClass().getSimpleName(), methodName, parameterType);
                        try {
                            m.invoke(
                                    element,
                                    createUnchecked(
                                            parameterType,
                                            data.get(CaseUtils.toCamelCase(methodName.substring(Constants.METHOD_SETTER_PREFIX.length()), true, '_')),
                                            attachment
                                    )
                            );
                        }
                        catch (Exception e) {
                            throw new ArgumentCreationException(e);
                        }
                    });
        }

        private void checkHref(Object element) {
            Arrays.stream(element.getClass().getMethods())
                    .filter(m -> m.getName().equals(Constants.METHOD_SET_HREF))
                    .findFirst().ifPresent(m -> {
                log.debug("Found set href method");
                try {
                    // TODO: maybe using toString() or getCid() it has to be configurable
                    m.invoke(element, attachment.toString());
                }
                catch (Exception e) {
                    throw new ArgumentCreationException(e);
                }
            });
        }

        static Object[] convertToObjectArray(Object array) {
            if (array.getClass().getComponentType().isPrimitive()) {
                int length = Array.getLength(array);
                Object[] result = new Object[length];

                for (int i = 0; i < length; i++) {
                    result[i] = Array.get(array, i);
                }

                return result;
            }
            else {
                return (Object[]) array;
            }
        }
    }


    /*
     * XML ELEMENT PROCESSOR: ARRAY PARAMETERS
     */

    private static class XmlElementProcessorArray extends XmlElementProcessor<Object[]> {

        public XmlElementProcessorArray(Object[] data, XRoadAttachment attachment) {
            super(data, attachment);
        }

        @Override
        public void processElement(Object element) {
            checkSetters(element);
        }


        @Override
        protected void checkSetters(Object element) {
            Arrays.stream(element.getClass().getMethods())
                    .filter(m -> m.getParameterCount() == 1
                            && m.getName().matches(Constants.PATTERN_SET_ARRAY)
                            && m.getParameterTypes()[0].isArray()).findFirst()
                    .ifPresent(m -> {
                        String methodName = m.getName();
                        Class<?> parameterType = m.getParameters()[0].getType().getComponentType();
                        log.debug("Found array setter method [{}]. Parameter type [{}]", methodName, parameterType);
                        try {
                            Object convertedArray = Array.newInstance(parameterType, data.length);
                            for (int i = 0; i < data.length; i++) {
                                Array.set(convertedArray, i, createUnchecked(parameterType, data[i], attachment));
                            }

                            m.invoke(element, convertedArray);
                        }
                        catch (Exception e) {
                            throw new ArgumentCreationException(e);
                        }
                    });
        }
    }

    /*
     * XML ELEMENT PROCESSOR FACTORY
     */

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class XmlElementProcessorFactory {

        public static XmlElementProcessorMap map(Map<String, Object> data, XRoadAttachment attachment) {
            return new XmlElementProcessorMap(data.entrySet().stream().collect(Collectors.toMap(
                    e -> CaseUtils.toCamelCase(e.getKey(), true, '_'), Map.Entry::getValue
            )), attachment);
        }

        public static XmlElementProcessorMap convertedMap(Map<String, Object> data, XRoadAttachment attachment) {
            return new XmlElementProcessorMap(data, attachment);
        }

        public static XmlElementProcessorArray array(Object[] data, XRoadAttachment attachment) {
            return new XmlElementProcessorArray(data, attachment);
        }

    }


}
