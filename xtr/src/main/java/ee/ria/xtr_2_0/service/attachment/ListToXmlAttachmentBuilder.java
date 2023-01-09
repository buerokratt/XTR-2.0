package ee.ria.xtr_2_0.service.attachment;

import com.nortal.jroad.model.XRoadAttachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MimeTypeUtils;

import java.util.Collection;
import java.util.Map;

@Slf4j
public class ListToXmlAttachmentBuilder extends BaseAttachmentBuilder {


    private static final String HEADER = "header";

    private static final String ELEMENT = "element";
    private static final String FOOTER = "footer";
    private static final String LIST_PROPERTY = "listProperty";

    /**
     * xml header section
     */
    private String header;
    /**
     * Format string for list elements example "<code>%s</code>"
     */
    private String element;
    /**
     * xml footer section
     */
    private String footer;

    /**
     * list section from which elements are aquired to fill the xml between header and footer
     */
    private String listProperty;

    /**
     * XRoadAttachment will be constructed with byte array from xml that is created from a list
     */
    @Override
    @SuppressWarnings("unchecked")
    public XRoadAttachment buildAttachment(Map<String, Object> parameters) {
        StringBuilder sb = new StringBuilder(header);

        if (parameters.containsKey(listProperty)) {
            Object listObject = parameters.get(listProperty);
            if (listObject instanceof Collection) {
                Collection<Object> list = (Collection<Object>) listObject;
                list.forEach(el -> sb.append(String.format(element, el)));
            }
        }

        sb.append(footer);

        if (log.isDebugEnabled()) {
            log.debug("Created attachment: {}", sb.toString());
        }

        return new XRoadAttachment(
                AttachmentIdGenerator.getId(),
                MimeTypeUtils.TEXT_XML_VALUE,
                getBytes(sb.toString())
        );
    }

    /**
     *
     * @param parameters contain information for xml generation
     * @return this builder with fields needed for xml generation initialized
     */
    @Override
    public AttachmentBuilder initBuilder(Map<String, Object> parameters) {
        if (parameters != null) {
            initModifiers(parameters);
            header = String.valueOf(parameters.get(HEADER));
            element = String.valueOf(parameters.get(ELEMENT));
            footer = String.valueOf(parameters.get(FOOTER));
            listProperty = String.valueOf(parameters.get(LIST_PROPERTY));
        }

        return this;
    }

}
