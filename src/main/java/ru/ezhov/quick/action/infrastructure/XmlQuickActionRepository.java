package ru.ezhov.quick.action.infrastructure;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.ezhov.quick.action.QuickAction;
import ru.ezhov.quick.action.QuickActionRepository;
import ru.ezhov.quick.action.QuickActionRepositoryException;
import ru.ezhov.quick.action.types.ActionType;
import ru.ezhov.quick.action.types.CopyToClipboardQuickAction;
import ru.ezhov.quick.action.types.GroupQuickAction;
import ru.ezhov.quick.action.types.OpenFileQuickAction;
import ru.ezhov.quick.action.types.OpenUrlQuickAction;
import ru.ezhov.quick.action.types.OpenUrlWithTextQuickAction;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlQuickActionRepository implements QuickActionRepository {

    private final InputStream inputStream;

    public XmlQuickActionRepository(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public List<QuickAction> actions() throws QuickActionRepositoryException {
        try {
            List<QuickAction> actions = new ArrayList<>();

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(inputStream);

            final NodeList actionsNodes = document.getChildNodes();
            int size = actionsNodes.getLength();
            for (int i = 0; i < size; i++) {
                Node actionsNode = actionsNodes.item(i);
                if ("actions".equals(actionsNode.getNodeName())) {
                    NodeList actionNode = actionsNode.getChildNodes();
                    for (int x = 0; x < actionNode.getLength(); x++) {
                        Node node = actionNode.item(x);
                        if ("action".equals(node.getNodeName())) {
                            actions.add(createAction(node));
                        }
                    }
                }
            }

            return actions;
        } catch (Exception ex) {
            throw new QuickActionRepositoryException("//TODO", ex);
        }
    }

    private QuickAction createAction(Node action) throws QuickActionRepositoryException {
        final Node type = action.getAttributes().getNamedItem("type");
        String typeAsString = type.getNodeValue();
        ActionType actionType = ActionType.valueOf(typeAsString);
        return QuickActionFactory.create(actionType, action);
    }

    private static class QuickActionFactory {
        static QuickAction createAction(Node action) throws QuickActionRepositoryException {
            final Node type = action.getAttributes().getNamedItem("type");
            String typeAsString = type.getNodeValue();
            ActionType actionType = ActionType.valueOf(typeAsString);
            return QuickActionFactory.create(actionType, action);
        }

        static QuickAction create(ActionType actionType, Node action) throws QuickActionRepositoryException {
            switch (actionType) {
                case OPEN_URL:
                    Map<String, String> mapOpenUrl = fieldsAndValues(action);
                    return new OpenUrlQuickAction(
                            mapOpenUrl.get("label"),
                            mapOpenUrl.get("description"),
                            mapOpenUrl.get("url")
                    );
                case OPEN_URL_WITH_TEXT:
                    Map<String, String> mapOpenUrlText = fieldsAndValues(action);
                    return new OpenUrlWithTextQuickAction(
                            mapOpenUrlText.get("label"),
                            mapOpenUrlText.get("description"),
                            mapOpenUrlText.get("baseUrl"),
                            mapOpenUrlText.get("placeholder")
                    );
                case OPEN_FILE:
                    Map<String, String> mapOpenFile = fieldsAndValues(action);
                    return new OpenFileQuickAction(
                            mapOpenFile.get("label"),
                            mapOpenFile.get("description"),
                            mapOpenFile.get("path")
                    );
                case COPY_TO_CLIPBOARD:
                    Map<String, String> mapCopy = fieldsAndValues(action);
                    return new CopyToClipboardQuickAction(
                            mapCopy.get("label"),
                            mapCopy.get("description"),
                            mapCopy.get("text")
                    );
                case GROUP:
                    List<QuickAction> actions = new ArrayList<>();
                    final NodeList childNodes = action.getChildNodes();
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        final Node node = childNodes.item(i);
                        if ("actions".equals(node.getNodeName())) {
                            NodeList groupActions = node.getChildNodes();
                            for (int x = 0; x < groupActions.getLength(); x++) {
                                Node n = groupActions.item(x);
                                if ("action".equals(n.getNodeName())) {
                                    actions.add(createAction(groupActions.item(x)));
                                }
                            }
                        }
                    }

                    Map<String, String> mapGroup = fieldsAndValues(action);
                    return new GroupQuickAction(
                            mapGroup.get("label"),
                            mapGroup.get("description"),
                            actions
                    );
                default:
                    throw new QuickActionRepositoryException("//TODO");
            }
        }

        private static Map<String, String> fieldsAndValues(Node action) {
            Map<String, String> map = new HashMap<>();
            final NodeList childNodes = action.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node node = childNodes.item(i);
                map.put(node.getNodeName(), node.getTextContent());
            }
            return map;
        }
    }
}
