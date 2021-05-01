package ru.ezhov.quick.action.infrastructure;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.yaml.snakeyaml.Yaml;
import ru.ezhov.quick.action.QuickActionRepository;
import ru.ezhov.quick.action.QuickActionRepositoryException;
import ru.ezhov.quick.action.contract.QuickAction;
import ru.ezhov.quick.action.types.ActionType;
import ru.ezhov.quick.action.types.CopyToClipboardQuickAction;
import ru.ezhov.quick.action.types.GroupQuickAction;
import ru.ezhov.quick.action.types.OpenFileQuickAction;
import ru.ezhov.quick.action.types.OpenUrlQuickAction;
import ru.ezhov.quick.action.types.OpenUrlWithTextHistoryQuickAction;
import ru.ezhov.quick.action.types.OpenUrlWithTextQuickAction;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YmlQuickActionRepository implements QuickActionRepository {

    private final InputStream inputStream;

    public YmlQuickActionRepository(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public List<QuickAction> actions() throws QuickActionRepositoryException {
        try {
            List<QuickAction> actions = new ArrayList<>();
            Yaml yaml = new Yaml();
            Map<String, Object> obj = yaml.load(inputStream);

            for (Map.Entry<String, Object> e : obj.entrySet()) {
                if ("actions".equals(e.getKey())) {
                    ArrayList<LinkedHashMap<String, Object>> linkedHashMaps =
                            (ArrayList<LinkedHashMap<String, Object>>) e.getValue();
                    for (LinkedHashMap<String, Object> l : linkedHashMaps) {
                        actions.add(createAction(l));
                    }
                }
            }
            return actions;
        } catch (Exception ex) {
            throw new QuickActionRepositoryException("//TODO", ex);
        }
    }

    private QuickAction createAction(LinkedHashMap<String, Object> action) throws QuickActionRepositoryException {
        String typeAsString = action.get("type").toString();
        ActionType actionType = ActionType.valueOf(typeAsString);
        return QuickActionFactory.create(actionType, action);
    }

    private static class QuickActionFactory {
        static QuickAction createAction(LinkedHashMap<String, Object> action) throws QuickActionRepositoryException {
            String typeAsString = action.get("type").toString();
            ActionType actionType = ActionType.valueOf(typeAsString);
            return QuickActionFactory.create(actionType, action);
        }

        static QuickAction create(ActionType actionType, LinkedHashMap<String, Object> action) throws QuickActionRepositoryException {
            switch (actionType) {
                case OPEN_URL:
                    return new OpenUrlQuickAction(
                            action.get("label").toString(),
                            action.get("description").toString(),
                            action.get("url").toString()
                    );
                case OPEN_URL_WITH_TEXT:
                    return new OpenUrlWithTextQuickAction(
                            action.get("label").toString(),
                            action.get("description").toString(),
                            action.get("baseUrl").toString(),
                            action.get("placeholder").toString()
                    );
                case OPEN_URL_WITH_TEXT_HISTORY:
                    return new OpenUrlWithTextHistoryQuickAction(
                            action.get("label").toString(),
                            action.get("description").toString(),
                            action.get("baseUrl").toString(),
                            action.get("placeholder").toString()
                    );
                case OPEN_FILE:
                    return new OpenFileQuickAction(
                            action.get("label").toString(),
                            action.get("description").toString(),
                            action.get("path").toString()
                    );
                case COPY_TO_CLIPBOARD:
                    return new CopyToClipboardQuickAction(
                            action.get("label").toString(),
                            action.get("description").toString(),
                            action.get("text").toString()
                    );
                case GROUP:
                    List<QuickAction> actions = new ArrayList<>();

                    ArrayList<LinkedHashMap<String, Object>> linkedHashMaps =
                            (ArrayList<LinkedHashMap<String, Object>>) action.get("actions");
                    for (LinkedHashMap<String, Object> a : linkedHashMaps) {
                        actions.add(createAction(a));
                    }
                    return new GroupQuickAction(
                            action.get("label").toString(),
                            action.get("description").toString(),
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
