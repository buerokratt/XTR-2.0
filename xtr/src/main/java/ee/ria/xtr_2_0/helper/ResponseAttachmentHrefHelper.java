package ee.ria.xtr_2_0.helper;

import ee.ria.xtr_2_0.exception.AttachmentCreationException;
import ee.ria.xtr_2_0.exception.HrefMissingException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseAttachmentHrefHelper {

    private static final String GENERIC_ERROR_MESSAGE = "Unable to get response attachment";

    /**
     * Tries to return href string value from the nested object according to the path provided.
     * @param path describes nested objects in content
     * @param content object containing other objects as described in path
     * @return string value of getHref method of the object at the end of the path
     *
     * @throws  HrefMissingException for cases where any object does not have appropriate getter methods, or getHref returns null
     * @throws  AttachmentCreationException for other cases of failure
     *
     * @see Constants#METHOD_GET_HREF exact name for getHref method mentioned above
     */
    public static String href(String path, Object content) {
        String[] split = path.split(Constants.RESPONSE_ATTACHMENT_HREF_PATH_SEPARATOR);
        for (String s : split) {
            try {
                Method getter = content.getClass().getMethod(
                        Constants.METHOD_GETTER_PREFIX + StringUtils.capitalize(s));
                content = getter.invoke(content);
            }
            catch (NoSuchMethodException e) {
                throw new HrefMissingException(path, content, e);
            }
            catch (Exception e) {
                throw new AttachmentCreationException(GENERIC_ERROR_MESSAGE, e);
            }
        }

        if (content == null) {
            throw new HrefMissingException(path, content);
        }

        try {
            Method getHrefMethod = content.getClass().getMethod(Constants.METHOD_GET_HREF);
            Object href = getHrefMethod.invoke(content);
            if (href == null) {
                throw new HrefMissingException(path, content);
            }

            return String.valueOf(href);
        }
        catch (NoSuchMethodException e) {
            throw new HrefMissingException(path, content, e);
        }
        catch (Exception e) {
            throw new AttachmentCreationException(GENERIC_ERROR_MESSAGE, e);
        }

    }

}
