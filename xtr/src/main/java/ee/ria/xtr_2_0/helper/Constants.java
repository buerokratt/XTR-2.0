package ee.ria.xtr_2_0.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    public static final String DATE_FORMAT = "dd.MM.yyyy";

    public static final String CHARSET_UTF_8 = "UTF-8";

    public static final String METHOD_SETTER_PREFIX = "set";
    public static final String METHOD_GETTER_PREFIX = "get";
    public static final String METHOD_ENUM_FOR_STRING = "forString";
    public static final String METHOD_NEW_INSTANCE = "newInstance";
    public static final String METHOD_ADD_NEW = "addNew";
    public static final String METHOD_REMOVE = "remove";
    public static final String METHOD_SEND = "send";
    public static final String METHOD_SET_HREF = "setHref";
    public static final String METHOD_GET_HREF = "getHref";

    public static final String CLASS_FACTORY = "Factory";


    public static final String PATTERN_SET_ARRAY = METHOD_SETTER_PREFIX + "[A-Z][a-zA-Z0-9]+Array";

    // attachment configuration

    public static final String ATTACHMENT_ROOT_ELEMENT_NAME = "attachmentRootElement";
    public static final String ATTACHMENT_HREF = "href";
    public static final String ATTACHMENT_DECODE_BASE64 = "decodeBase64";
    public static final String ATTACHMENT_CONTENT_MODIFIERS = "content";
    public static final String ATTACHMENT_NESTED_ARRAYS = "nestedArrays";
    public static final String ATTACHMENT_IGNORE_FIELDS= "ignoreFields";

    public static final String RESPONSE_ATTACHMENT_HREF_PATH_SEPARATOR = "#";

}
