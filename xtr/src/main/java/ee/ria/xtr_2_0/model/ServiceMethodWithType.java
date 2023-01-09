package ee.ria.xtr_2_0.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.xmlbeans.XmlObject;

import java.lang.reflect.Method;

@RequiredArgsConstructor
@Data
public class ServiceMethodWithType {

    private final Method method;
    private final Class<XmlObject> argumentType;

}
