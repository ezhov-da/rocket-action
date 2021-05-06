package ru.ezhov.quick.action.infrastructure;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.yaml.snakeyaml.Yaml;
import ru.ezhov.quick.action.QuickActionRepository;
import ru.ezhov.quick.action.QuickActionRepositoryException;
import ru.ezhov.quick.action.types.ActionType;
import ru.ezhov.quick.action.types.CopyToClipboardQuickAction;
import ru.ezhov.quick.action.types.GroupQuickAction;
import ru.ezhov.quick.action.types.OpenFileQuickAction;
import ru.ezhov.quick.action.types.OpenUrlQuickAction;
import ru.ezhov.quick.action.types.OpenUrlWithTextHistoryQuickAction;
import ru.ezhov.quick.action.types.OpenUrlWithTextQuickAction;
import ru.ezhov.quick.action.types.SeparatorQuickAction;
import ru.ezhov.quick.action.types.ShowImageQuickAction;

import java.awt.Component;
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
    public List<Component> actions() throws QuickActionRepositoryException {
        try {
            List<Component> actions = new ArrayList<>();
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

    private Component createAction(LinkedHashMap<String, Object> action) throws QuickActionRepositoryException {
        String typeAsString = action.get("type").toString();
        ActionType actionType = ActionType.valueOf(typeAsString);
        return QuickActionFactory.create(actionType, action);
    }

    private static class QuickActionFactory {
        static Component createAction(LinkedHashMap<String, Object> action) throws QuickActionRepositoryException {
            String typeAsString = action.get("type").toString();
            ActionType actionType = ActionType.valueOf(typeAsString);
            return QuickActionFactory.create(actionType, action);
        }

        static Component create(ActionType actionType, LinkedHashMap<String, Object> action) throws QuickActionRepositoryException {
            switch (actionType) {
                case OPEN_URL:
                    return new OpenUrlQuickAction().create(action);
                case OPEN_URL_WITH_TEXT:
                    return new OpenUrlWithTextQuickAction().create(action);
                case OPEN_URL_WITH_TEXT_HISTORY:
                    return new OpenUrlWithTextHistoryQuickAction().create(action);
                case OPEN_FILE:
                    return new OpenFileQuickAction().create(action);
                case COPY_TO_CLIPBOARD:
                    return new CopyToClipboardQuickAction().create(action);
                case SHOW_IMAGE:
                    return new ShowImageQuickAction().create(action);
                case SEPARATOR:
                    return new SeparatorQuickAction().create(action);
                case GROUP:
                    List<Component> components = new ArrayList<>();

                    ArrayList<LinkedHashMap<String, Object>> linkedHashMaps =
                            (ArrayList<LinkedHashMap<String, Object>>) action.get("actions");
                    for (LinkedHashMap<String, Object> a : linkedHashMaps) {
                        components.add(createAction(a));
                    }
                    action.put("components", components);
                    return new GroupQuickAction().create(action);
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
