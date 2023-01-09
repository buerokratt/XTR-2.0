package ee.ria.xtr_2_0.model;

import com.nortal.jroad.model.XRoadAttachment;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.xmlbeans.XmlObject;

@RequiredArgsConstructor
@Data
public class ConvertedRequest<T extends XmlObject> {

    private final T request;

    private final XRoadAttachment attachment;

    public ConvertedRequest(T request) {
        this(request, null);
    }
}
