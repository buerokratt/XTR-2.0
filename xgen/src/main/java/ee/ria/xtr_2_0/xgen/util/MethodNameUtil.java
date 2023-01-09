package ee.ria.xtr_2_0.xgen.util;

import com.nortal.jroad.model.XmlBeansXRoadMetadata;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.impl.common.NameUtil;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MethodNameUtil {

    /**
     * Generates method names.
     */
    public static String methodName(XmlBeansXRoadMetadata metadata) {
        String version = metadata.getVersion();
        String methodName = StringUtils.isBlank(version) ?
                metadata.getOperationName() : metadata.getOperationName() + "_" + version;
        return NameUtil.lowerCamelCase(methodName);
    }

}
