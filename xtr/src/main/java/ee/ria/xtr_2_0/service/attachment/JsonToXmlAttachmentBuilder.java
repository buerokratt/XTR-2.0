package ee.ria.xtr_2_0.service.attachment;

import com.google.common.collect.Maps;
import com.nortal.jroad.model.XRoadAttachment;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import ee.ria.xtr_2_0.helper.Constants;
import ee.ria.xtr_2_0.helper.ObjectStreamHelper;
import org.springframework.util.MimeTypeUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonToXmlAttachmentBuilder extends BaseAttachmentBuilder {

    private static final String ROOT_ELEMENT = "rootElement";

    private String rootElement;

    private Map<String, String> nestedArrays;

    private Set<String> ignoredFields;

    /**
     * XRoadAttachment will be constructed with byte array from xml that is created from intermediate json string.
     * This allows for construction of nested XML.
     */
    @Override
    public XRoadAttachment buildAttachment(Map<String, Object> parameters) {
        XStream xstream = new XStream(new DomDriver(Constants.CHARSET_UTF_8, new XmlFriendlyReplacer("_", "_")));
        xstream.alias(rootElement, Map.class);
        xstream.registerConverter(new MapConverter());
        return new XRoadAttachment(
                AttachmentIdGenerator.getId(),
                MimeTypeUtils.TEXT_XML_VALUE,
                getBytes(xstream.toXML(ignoredFields != null ? filterParameters(parameters) : parameters))
        );
    }

    private Map<String, Object> filterParameters(Map<String, Object> parameters) {
        Map<String, Object> filtered = Maps.newHashMap();
        parameters.forEach((key, value) -> {
            if (!ignoredFields.contains(key)) {
                filtered.put(key, value);
            }
        });

        return filtered;
    }

    @Override
    public AttachmentBuilder initBuilder(Map<String, Object> parameters) {
        if (parameters != null) {
            initModifiers(parameters);
            rootElement = String.valueOf(parameters.get(ROOT_ELEMENT));
            if (parameters.containsKey(Constants.ATTACHMENT_NESTED_ARRAYS)) {
                nestedArrays = Maps.newHashMap();
                Arrays.stream(String.valueOf(parameters.get(Constants.ATTACHMENT_NESTED_ARRAYS)).split(","))
                        .forEach(s -> {
                            String[] split = s.split(Constants.RESPONSE_ATTACHMENT_HREF_PATH_SEPARATOR);
                            nestedArrays.put(split[0], split[1]);
                        });
            }

            if (parameters.containsKey(Constants.ATTACHMENT_IGNORE_FIELDS)) {
                ignoredFields = ObjectStreamHelper.stream(parameters.get(Constants.ATTACHMENT_IGNORE_FIELDS))
                        .map(String::valueOf)
                        .collect(Collectors.toSet());
            }
        }

        return this;
    }

    /**
     * Provides json marshalling of mapped content.
     */
    public class MapConverter implements Converter {

        @Override
        @SuppressWarnings("unchecked")
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            Map<String, Object> map = (Map<String, Object>) source;
            map.forEach((key, value) -> {
                if(value==null){
                    return;
                }
                Class<?> valueClass = value.getClass();
                if (Collection.class.isAssignableFrom(valueClass) || valueClass.isArray()) {
                    if (nestedArrays != null && nestedArrays.containsKey(key)) {
                        writer.startNode(key);
                        ObjectStreamHelper.stream(value).forEach(o -> {
                            writer.startNode(nestedArrays.get(key));
                            marshal(o, writer, context);
                            writer.endNode();
                        });
                        writer.endNode();
                    }
                    else {
                        ObjectStreamHelper.stream(value).forEach(o -> writeElement(writer, key, o));
                    }
                }
                else if (Map.class.isAssignableFrom(valueClass)) {
                    writer.startNode(key);
                    marshal(value, writer, context);
                    writer.endNode();
                }
                else {
                    writeElement(writer, key, value);
                }
            });
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            // noop
            return null;
        }

        @Override
        public boolean canConvert(Class type) {
            return Map.class.isAssignableFrom(type);
        }

        private void writeElement(HierarchicalStreamWriter writer, String key, Object value) {
            writer.startNode(key);
            writer.setValue(String.valueOf(value));
            writer.endNode();
        }
    }

}
