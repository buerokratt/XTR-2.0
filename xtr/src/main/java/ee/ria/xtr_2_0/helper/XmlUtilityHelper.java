package ee.ria.xtr_2_0.helper;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XmlUtilityHelper {

    /**
     * Converts NodeList to List of Nodes
     * @param n to be converted
     * @return conversion result
     */
    public static List<Node> asList(NodeList n) {
        return n.getLength() == 0 ? Lists.newArrayList() : new NodeListWrapper(n);
    }

    static final class NodeListWrapper extends AbstractList<Node> implements RandomAccess {

        private final NodeList list;

        NodeListWrapper(NodeList list) {
            this.list = list;
        }

        public Node get(int index) {
            return list.item(index);
        }

        public int size() {
            return list.getLength();
        }
    }

}
