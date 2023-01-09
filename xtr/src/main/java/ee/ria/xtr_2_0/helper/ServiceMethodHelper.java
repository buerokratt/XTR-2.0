package ee.ria.xtr_2_0.helper;

import com.nortal.jroad.model.XRoadMessage;
import ee.ria.xtr_2_0.exception.ServiceMethodNotDistinguishableException;
import ee.ria.xtr_2_0.model.ServiceMethodWithType;
import ee.ria.xtr_2_0.model.XtrDatabase;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.XmlObject;

import java.lang.reflect.Method;
import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ServiceMethodHelper {

    /**
     * Tries to return service method with type.
     * @param serviceBean bean containing an X-Road service methods
     * @param xtrDatabase provides mapping of request parameters to service bean
     * @return contains method and its first argument's type
     * @throws ServiceMethodNotDistinguishableException when not being able to return  unique method
     */
    @SuppressWarnings("unchecked")
    public static ServiceMethodWithType findMethod(Object serviceBean, XtrDatabase xtrDatabase) {
        Method[] methods = Arrays.stream(serviceBean.getClass().getMethods())
                .filter(method -> method.getName().equals(xtrDatabase.getMethod()) && method.getParameterCount() == 2)
                .toArray(Method[]::new);

        if (methods.length != 1) {
            throw new ServiceMethodNotDistinguishableException(xtrDatabase.getServiceName(), xtrDatabase.getMethod());
        }

        Method method = xtrDatabase.isAttachmentNotEmpty() ? findSendMethod(serviceBean.getClass()) : methods[0];
        log.debug("Method: {}", method);
        return new ServiceMethodWithType(method, (Class<XmlObject>) methods[0].getParameterTypes()[0]);
    }

    private static Method findSendMethod(Class<?> beanClass) {
        Method result = null;
        if (beanClass != null) {
            try {
                result = beanClass.getDeclaredMethod(Constants.METHOD_SEND, XRoadMessage.class,
                        String.class, String.class, String.class);
            }
            catch (NoSuchMethodException e) {
                result = findSendMethod(beanClass.getSuperclass());
            }
        }

        return result;
    }

}
