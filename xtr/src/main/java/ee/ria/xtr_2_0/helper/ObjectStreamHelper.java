package ee.ria.xtr_2_0.helper;

import ee.ria.xtr_2_0.exception.AttachmentCreationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Helper to produce streams from Objects.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectStreamHelper {

    /**
     * Returns stream if input object is an array or a Collection.
     * @param o objet to be converted to Stream
     * @return stream of o
     * @throws AttachmentCreationException with message notifying that the input was not an array or collection
     */
    @SuppressWarnings("unchecked")
    public static Stream<Object> stream(Object o) {
        if (Collection.class.isAssignableFrom(o.getClass())) {
            return ((Collection<Object>) o).stream();
        }
        else if (o.getClass().isArray()) {
            return Arrays.stream((Object[]) o);
        }
        else {
            throw new AttachmentCreationException(o.getClass() + " is not array nor collection");
        }
    }

}
