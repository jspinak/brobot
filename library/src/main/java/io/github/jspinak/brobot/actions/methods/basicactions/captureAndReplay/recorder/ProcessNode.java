package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.recorder;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Processes the node in the XML Document containing user interactions.
 *
 * This interface can be implemented in many ways, as it's not clear how to simplify the raw data. When there
 *   are multiple classes implementing this interface, the IDE could allow users to select one of them to
 *   use for processing. The initial implementation provided uses pauses between user inputs to determine
 *   which inputs are pivotal inputs. Developers are welcome to code new implementations of ProcessNode.
 */
public interface ProcessNode {
    void populateNodeList(NodeList childNodes, RecordInputsXML doc);
}
