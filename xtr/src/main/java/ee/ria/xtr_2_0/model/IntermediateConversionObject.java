package ee.ria.xtr_2_0.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import ee.ria.xtr_2_0.helper.IntermediateConversionObjectJsonSerializer;
import lombok.Data;

import java.util.List;

@JsonSerialize(using = IntermediateConversionObjectJsonSerializer.class)
@Data
public class IntermediateConversionObject {

    private String objectKey;
    private String singleValue;
    private List<IntermediateConversionObject> properties;
    private List<IntermediateConversionObject> collection;
    private boolean writeRootElementAsArray;
    private boolean isCollectionElement;

    public IntermediateConversionObject getPropertyByName(String objectKey) {
        return properties != null ? properties.stream().filter(property -> objectKey.equals(property.getObjectKey()))
                .findFirst().orElse(null) : null;
    }

    public boolean isSingleElement() {
        return (collection == null || collection.isEmpty()) && (properties == null || properties.isEmpty());
    }

    public boolean hasProperties() {
        return getProperties() != null && !getProperties().isEmpty();
    }

    /**
     * adds an element to collection
     *
     * @param intermediateConversionObject element to add
     */
    private void addCollectionElement(IntermediateConversionObject intermediateConversionObject) {
        if (collection == null) {
            collection = Lists.newArrayList();
        }

        collection.add(intermediateConversionObject);
    }

    /**
     * adds a property to properties list
     *
     * @param intermediateConversionObject property to add.
     */
    public void addProperty(IntermediateConversionObject intermediateConversionObject) {
        if (properties == null) {
            properties = Lists.newArrayList();
        }
        properties.add(intermediateConversionObject);
    }

    /**
     * Adds an element as a collection type of elements (collection types are transformed into
     * JSON array elements later on.
     *
     * @param element element to add as collection.
     */
    public void addElementAsCollection(IntermediateConversionObject element) {
        IntermediateConversionObject propertyByName = getPropertyByName(element.getObjectKey());

        if (propertyByName != null) {
            propertyByName.addCollectionElement(element);
        }
        else {
            initCollectionElement(element);
        }
    }

    private void initCollectionElement(IntermediateConversionObject firstCollectionElem) {
        IntermediateConversionObject tempCollectionProperty = new IntermediateConversionObject();
        tempCollectionProperty.setCollectionElement(true);

        tempCollectionProperty.setObjectKey(firstCollectionElem.getObjectKey());
        tempCollectionProperty.addCollectionElement(firstCollectionElem);
        addProperty(tempCollectionProperty);
    }
}
