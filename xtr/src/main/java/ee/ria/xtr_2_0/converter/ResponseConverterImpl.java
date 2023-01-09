package ee.ria.xtr_2_0.converter;

import com.google.common.collect.Lists;
import com.nortal.jroad.client.util.XmlBeansUtil;
import com.nortal.jroad.model.XRoadAttachment;
import com.nortal.jroad.model.XRoadMessage;
import ee.ria.xtr_2_0.exception.AttachmentCreationException;
import ee.ria.xtr_2_0.exception.AttachmentMissingException;
import ee.ria.xtr_2_0.exception.ResponseContentMissingException;
import ee.ria.xtr_2_0.exception.ResponseCreationException;
import ee.ria.xtr_2_0.exception.ResponseRootNodeNotConfiguredException;
import ee.ria.xtr_2_0.helper.Constants;
import ee.ria.xtr_2_0.helper.ResponseAttachmentHrefHelper;
import ee.ria.xtr_2_0.helper.XmlUtilityHelper;
import ee.ria.xtr_2_0.model.IntermediateConversionObject;
import ee.ria.xtr_2_0.model.XtrAttachment;
import ee.ria.xtr_2_0.model.XtrDatabase;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Collection;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

@Component
@Slf4j
public class ResponseConverterImpl implements ResponseConverter {

    private static final String NODE_PATH_DELIMITER = "/";

    @Value("${xtr.response.root.nodes}")
    private Collection<String> possibleResponseRootNodes = Lists.newArrayList();

    /**
     * Get the final map object for JSON mapper.
     *
     * @param soapResponse source of response data received from Xroad
     * @return map with request and response data
     */
    public IntermediateConversionObject convert(XmlObject soapResponse, XtrDatabase databaseConf) {
        log.debug("Converting SOAP response: {}", soapResponse);
        if (soapResponse == null) {
            return null;
        }

        Node rootNode = soapResponse.getDomNode();
        NodeList nodeList = rootNode.getChildNodes();
        IntermediateConversionObject result;

        // prepare XML cursor to iterate over XML response
        XmlCursor responseCursor = soapResponse.newCursor();

        String respRootNode = databaseConf.getResponseRootNode();
        if (isNotConfigured(respRootNode)) {
            log.debug("Response root node not configured: {}:{}",
                    databaseConf.getRegistryCode(), databaseConf.getServiceCode());
            // try to determine root node
            QName qName = responseCursor.getName();
            respRootNode = XmlUtilityHelper.asList(nodeList).stream().map(Node::getLocalName)
                    .filter(n -> possibleResponseRootNodes.contains(n))
                    .findFirst().orElse(qName != null ? qName.getLocalPart() : null);
            log.debug("Possible root node '{}'", respRootNode);
        }

        if (isNotConfigured(respRootNode)) {
            throw new ResponseRootNodeNotConfiguredException(databaseConf);
        }


        if (!responseCursor.toChild(new QName(databaseConf.getNamespaceUri(), respRootNode))) {
            responseCursor.toChild(respRootNode);
        }

        responseCursor.toFirstChild();

        final String finalRespRootNode = respRootNode;

        // handle the case when the response has 2+ nesting
        // (i.e. inside something like <response></response> tags inside Response tags)
        result = XmlUtilityHelper.asList(nodeList).stream()
                .filter(node -> finalRespRootNode.equals(node.getLocalName()))
                .findFirst()
                .map(node -> getJsonObjectFromNode(node, responseCursor, finalRespRootNode,
                        databaseConf.getNamespaceUri(), null, true))
                .orElse(null);

        // handle the case when the response has the minimal nesting (i.e. response starts right after Body tag)
        if (result == null && respRootNode.equals(rootNode.getLocalName())) {
            result = getJsonObjectFromNode(rootNode, responseCursor, respRootNode,
                    databaseConf.getNamespaceUri(), null, false);
        }

        responseCursor.dispose();

        // check is for empty response body or response body with a single empty child that is the same as response node
        if (result != null && result.isSingleElement()
                && result.getObjectKey().equals(respRootNode)
                && result.getSingleValue() == null) {
            return null;
        }

        log.debug("Converted SOAP result: {}", result);
        return result;
    }

    @Override
    public IntermediateConversionObject convertAttachment(XRoadMessage<?> message, XtrDatabase database) {
        XtrAttachment attachments = database.getAttachments();
        return convertAttachment(getAttachmentString(message, attachments), attachments);
    }

    private <T> InputStream getAttachmentString(XRoadMessage<T> message, XtrAttachment attachment) {
        // TODO: what if there's more than one attachment? Is that even a case?
        T content = message.getContent();
        if (content == null) {
            throw new ResponseContentMissingException();
        }

        // swaRef
        if (message.getAttachments().isEmpty() && content instanceof XmlObject) {
            try {
                XmlObject casted = (XmlObject) content;
                for (XmlObject obj : XmlBeansUtil.getAllObjects(casted)) {
                    for (Method m : XmlBeansUtil.getSwaRefGetters(obj)) {
                        DataHandler dataHandler = (DataHandler) m.invoke(obj);
                        return dataHandler.getInputStream();
                    }
                }
            }
            catch (Exception e) {
                throw new AttachmentCreationException("Unable to create attachment", e);
            }
        }

        validateResponseAttachmentConf(Constants.ATTACHMENT_HREF, attachment);

        String href = ResponseAttachmentHrefHelper.href(
                String.valueOf(attachment.getResponse().get(Constants.ATTACHMENT_HREF)), content);

        Object decodeBase64Obj = attachment.getResponse().get(Constants.ATTACHMENT_DECODE_BASE64);
        boolean decodeBase64 = decodeBase64Obj != null && (decodeBase64Obj instanceof Boolean ?
                (boolean) decodeBase64Obj : Boolean.parseBoolean(String.valueOf(decodeBase64Obj)));

        return getAttachmentFromHref(message, href, decodeBase64);
    }

    private InputStream getAttachmentFromHref(XRoadMessage<?> response, String href, boolean decodeBase64) {
        if (href == null || response.getAttachments() == null) {
            throw new AttachmentMissingException();
        }

        String attachmentCid = href.startsWith("cid:") ? href.substring("cid:".length()) : href;

        XRoadAttachment xRoadAttachment = response.getAttachments().stream()
                .filter(attachment -> attachmentCid.equals(attachment.getCid()))
                .findFirst()
                .orElseThrow(AttachmentMissingException::new);
        try {

            return decodeBase64
                    ? decompressGzipBytes(Base64.getDecoder().decode(xRoadAttachment.getData()))
                    : decompressGzipBytes(xRoadAttachment.getData());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static InputStream decompressGzipBytes(byte[] str) throws Exception {
        if (str == null) {
            return null;
        }

        try {
            return new GZIPInputStream(new ByteArrayInputStream(str));
        }
        catch (ZipException e) {
            return new ByteArrayInputStream(str);
        }
    }

    // package private for testing purposes
    IntermediateConversionObject convertAttachment(InputStream is, XtrAttachment attachment) {
        validateResponseAttachmentConf(Constants.ATTACHMENT_ROOT_ELEMENT_NAME, attachment);

        String attachmentRootElementName = String.valueOf(attachment.getResponse()
                .get(Constants.ATTACHMENT_ROOT_ELEMENT_NAME));

        XmlObject xmlObject;
        try {
            xmlObject = XmlObject.Factory.parse(is);
        }
        catch (Exception e) {
            throw new ResponseCreationException(e);
        }

        XmlCursor cursor = xmlObject.newCursor();
        cursor.toChild(attachmentRootElementName);

        // TODO: pay attention to this isNested variable. It actually may be nested somewhere
        return getJsonObjectFromNode(cursor.getDomNode(), cursor, attachmentRootElementName, null, null, false);
    }

    private void validateResponseAttachmentConf(String property, XtrAttachment attachment) {
        if (attachment == null || attachment.getResponse() == null
                || !attachment.getResponse().containsKey(property)) {
            throw new AttachmentCreationException(property + " is not configured", attachment);
        }
    }

    /**
     * Recursively convert raw DOM objects to IntermediateConversionObject instances to easily
     * split the incoming data to different types.
     *
     * @param singleNode node to be processed
     * @param isNested   this flag represents whether response is located near the body (false) or inside some nested tags
     * @return ready IntermediateConversionObject instance
     */
    private static IntermediateConversionObject getJsonObjectFromNode(
            Node singleNode, XmlCursor cursor, String rootElementName,
            String rootElementNamespace, IntermediateConversionObject parent, boolean isNested) {

        if (singleNode == null || singleNode.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }

        IntermediateConversionObject result = new IntermediateConversionObject();

        result.setObjectKey(singleNode.getLocalName());
        boolean collectionElement = isCollectionElement(
                getFullNodePath(
                        singleNode,
                        isNested,
                        rootElementName),
                cursor,
                singleNode.getLocalName(),
                isNested,
                rootElementName,
                rootElementNamespace);
        result.setCollectionElement(collectionElement);

        // second check is for empty response body or response body with a single empty closed child tag/node
        if (isSingleValue(singleNode) && !singleNode.getLocalName().equals(rootElementName)) {
            //scalar value
            String nodeValue = singleNode.getChildNodes().item(0).getNodeValue();
            if (nodeValue == null || !nodeValue.trim().isEmpty()) {
                // Somewhat a hack, because there is a suspicion that x-road tries to return a pretty print version of XML.
                // Thus if text value is null then value tag/node is empty closed: <anemptyelement />.
                // But if array value is null or empty then tag/node non-empty closed: <anemptyarray>\n     </anemptyarray>
                // with a new line character and spaces that tabulate the first tag.
                // We trim all of the blank characters and if the value is empty then ignore it in parsing.
                result.setSingleValue(nodeValue);
            }

        }
        else if (singleNode.getChildNodes() != null) {

            // is not a collection of properties
            NodeList childNodes = singleNode.getChildNodes();

            // go through the list of properties
            for (Node node : XmlUtilityHelper.asList(childNodes)) {
                IntermediateConversionObject jsonObjectFromNode = getJsonObjectFromNode(node, cursor, rootElementName,
                        rootElementNamespace, result, isNested);
                // add a property only if it is not a collection (collections are handled one recursive level deeper)
                if (jsonObjectFromNode != null && !jsonObjectFromNode.isCollectionElement()) {
                    result.addProperty(jsonObjectFromNode);
                }
            }
        }

        // to determine if an element is a collectionElement or not
        // we need to go one invocation level deeper (cause maxOccurs='unbounded' is present on the
        // child elements, not parent elements in XML)
        // check if the element should be added as collection
        if (collectionElement && parent != null) {
            parent.addElementAsCollection(result);
        }

        return result;
    }

    /**
     * Get the full node path (i.e. with nesting) divided by NODE_PATH_DELIMITER symbol.
     *
     * @param node             default node object
     * @param isNested         this flag represents whether response is located near the body (false) or inside some nested tags
     * @param responseRootNode response root node name
     * @return full path string
     */
    private static String getFullNodePath(Node node, boolean isNested, String responseRootNode) {
        StringBuilder fullNodeName = new StringBuilder(node.getLocalName());
        Node parentNode = node.getParentNode();

        while (parentNode != null && parentNode.getLocalName() != null
                && !parentNode.getLocalName().equals(responseRootNode)
                && !parentNode.getLocalName().equals("#document")) {
            fullNodeName.insert(0, parentNode.getLocalName() + NODE_PATH_DELIMITER);
            parentNode = parentNode.getParentNode();
        }

        if (!isNested) {
            fullNodeName.insert(0, responseRootNode + NODE_PATH_DELIMITER);
        }

        return fullNodeName.toString()
                .replace(responseRootNode + NODE_PATH_DELIMITER + responseRootNode, responseRootNode);
    }

    /**
     * Check if XML schema for the given type has maxOccurs="unbounded" value.
     *
     * @param nodeName             name of node to be processed
     * @param responseCursor       default cursor instance pointing at the root of response schema
     * @param isNested             this flag represents whether response is located near the body (false) or inside some nested tags
     * @param responseRootNode     response root node name
     * @param elementsNamespaceURI namespace URI
     * @return boolean response
     */
    private static boolean isCollectionElement(String nodeName, XmlCursor responseCursor, String localName,
                                               boolean isNested, String responseRootNode, String elementsNamespaceURI) {
        XmlCursor localResponseCursor = responseCursor.newCursor();

        if (!isNested && !responseCursor.getObject().getDomNode().getLocalName().equals(responseRootNode)) {
            localResponseCursor.toParent();
        }

        // get the related XML schema object by node (i.e. element) name
        boolean hasSuchNode = false;

        // attempting to search for a child of unknown depth
        if (nodeName.contains(NODE_PATH_DELIMITER)) {
            String[] childrenNames = nodeName.split(NODE_PATH_DELIMITER);

            boolean isRoot = true;

            for (String childName : childrenNames) {
                if (isRoot) {
                    hasSuchNode = localResponseCursor.toNextSibling(new QName(elementsNamespaceURI, childName));
                    if (!hasSuchNode) {
                        hasSuchNode = localResponseCursor.toNextSibling(childName);
                    }

                    isRoot = false;
                }
                else {
                    hasSuchNode = localResponseCursor.toChild(elementsNamespaceURI, childName);
                    if (!hasSuchNode) {
                        hasSuchNode = localResponseCursor.toChild(childName);
                    }
                }
            }

        }
        else {
            // if node was found on the first nesting level
            hasSuchNode = localResponseCursor.toNextSibling(new QName(elementsNamespaceURI, nodeName));
            if (!hasSuchNode) {
                hasSuchNode = localResponseCursor.toNextSibling(nodeName);
            }
        }

        localResponseCursor.toParent();

        // count the number of elements with the same name
        // if we have more than one, than it should form a collection element
        long count = XmlUtilityHelper.asList(localResponseCursor.getDomNode().getChildNodes())
                .stream()
                .filter(node -> node != null
                        && node.getNodeType() == Node.ELEMENT_NODE
                        && node.getLocalName() != null
                        && node.getLocalName().equals(localName))
                .count();

        if (count > 1) {
            return true;
        }

        SchemaProperty property = localResponseCursor.getObject().schemaType()
                .getElementProperty(new QName(elementsNamespaceURI, localName));

        if (property == null) {
            property = localResponseCursor.getObject().schemaType().getElementProperty(new QName(localName));
        }

        localResponseCursor.dispose();

        if (hasSuchNode && property != null) {
            BigInteger maxOccurs = property.getMaxOccurs();

            // if maxOccurs equals null or bigger than 1, then we have a collection, not a map or string
            return (maxOccurs == null || maxOccurs.compareTo(BigInteger.ONE) > 0);
        }

        return false;
    }

    /**
     * Check if the node is alone on its nesting level.
     *
     * @param singleNode node to be processed
     * @return boolean response
     */
    private static boolean isSingleValue(Node singleNode) {
        NodeList childNodes = singleNode.getChildNodes();
        return childNodes == null || (childNodes.getLength() == 1 && !childNodes.item(0).hasChildNodes());
    }

    private static boolean isNotConfigured(String respRootNode) {
        return StringUtils.isEmpty(respRootNode) || "null".equals(respRootNode);
    }

}
