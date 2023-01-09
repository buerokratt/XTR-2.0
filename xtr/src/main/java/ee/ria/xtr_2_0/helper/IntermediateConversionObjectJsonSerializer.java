package ee.ria.xtr_2_0.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ee.ria.xtr_2_0.model.IntermediateConversionObject;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Defines how InterMediateConversionObject will be serialized.
 * @see IntermediateConversionObject
 * @see ee.ria.xtr_2_0.model.XtrResponse contains ICO, will be serialized when returned
 */
public class IntermediateConversionObjectJsonSerializer extends JsonSerializer<IntermediateConversionObject> {

    /**
     * Serialization implementation.
     * @param value will be serialized
     * @param gen  used for generating json
     * @param serializers provides serializers for specific types
     * @throws IOException
     */
    @Override
    public void serialize(IntermediateConversionObject value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();

        if (value.hasProperties()) {
            value.getProperties().stream().filter(Objects::nonNull).forEach(element -> {
                try {
                    if (element.isSingleElement()) {
                        // simple property
                        gen.writeStringField(element.getObjectKey(), element.getSingleValue());
                    }
                    else if (element.isCollectionElement()) {
                        // collection element
                        writeCollectionElement(element, gen);
                    }
                    else {
                        // this is a nested object
                        gen.writeObjectField(element.getObjectKey(), element);
                    }
                }
                catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
        }
        else {
            gen.writeStringField(value.getObjectKey(), value.getSingleValue());
        }

        gen.writeEndObject();
    }

    private void writeCollectionElement(IntermediateConversionObject value, JsonGenerator gen) throws IOException {
        gen.writeArrayFieldStart(value.getCollection().get(0).getObjectKey());

        boolean isSimpleValueList = value.getCollection().stream()
                .allMatch(IntermediateConversionObject::isSingleElement);

        Consumer<IntermediateConversionObject> consumer = isSimpleValueList ?
                element ->
                {
                    // outputs primitive elements
                    try {
                        gen.writeString(element.getSingleValue());
                    }
                    catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
                :
                element ->
                {
                    // outputs object elements
                    try {
                        gen.writeObject(element);
                    }
                    catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                };

        value.getCollection().forEach(consumer);

        gen.writeEndArray();
    }
}
