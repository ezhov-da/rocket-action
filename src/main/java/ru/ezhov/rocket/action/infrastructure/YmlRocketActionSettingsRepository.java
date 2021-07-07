package ru.ezhov.rocket.action.infrastructure;

import org.yaml.snakeyaml.Yaml;
import ru.ezhov.rocket.action.RocketActionSettingsRepository;
import ru.ezhov.rocket.action.RocketActionSettingsRepositoryException;
import ru.ezhov.rocket.action.api.RocketActionSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class YmlRocketActionSettingsRepository implements RocketActionSettingsRepository {

    private final URI uri;
    private static final String TYPE = "type";
    private static final String ID = "_id";
    private static final String ACTIONS = "actions";

    public YmlRocketActionSettingsRepository(URI uri) {
        this.uri = uri;
    }

    @Override
    public List<RocketActionSettings> actions() throws RocketActionSettingsRepositoryException {
        try (InputStream inputStream = uri.toURL().openStream()) {
            List<RocketActionSettings> actions = new ArrayList<>();
            Yaml yaml = new Yaml();
            Map<String, Object> obj = yaml.load(inputStream);

            for (Map.Entry<String, Object> e : obj.entrySet()) {
                if (ACTIONS.equals(e.getKey())) {
                    ArrayList<LinkedHashMap<String, Object>> linkedHashMaps =
                            (ArrayList<LinkedHashMap<String, Object>>) e.getValue();
                    for (LinkedHashMap<String, Object> l : linkedHashMaps) {
                        actions.add(createAction(l));
                    }
                }
            }
            return actions;
        } catch (Exception ex) {
            throw new RocketActionSettingsRepositoryException("//TODO", ex);
        }
    }

    @Override
    public void save(List<RocketActionSettings> settings) throws RocketActionSettingsRepositoryException {
        File file = new File(uri.getPath());
        try (OutputStreamWriter outputStreamWriter =
                     new OutputStreamWriter(
                             new FileOutputStream(file),
                             StandardCharsets.UTF_8)
        ) {
            Yaml yaml = new Yaml();

            List<Map<String, Object>> all = new ArrayList<>();
            recursiveSettings(settings, all);

            Map<String, Object> map = new LinkedHashMap<>();
            map.put(ACTIONS, all);
            yaml.dump(map, outputStreamWriter);
        } catch (Exception ex) {
            throw new RocketActionSettingsRepositoryException("//TODO", ex);
        }
    }

    private void recursiveSettings(List<RocketActionSettings> settings, List<Map<String, Object>> actions) {
        for (RocketActionSettings data : settings) {
            Map<String, Object> object = new LinkedHashMap<>();
            object.put(TYPE, data.type());
            object.put(ID, data.id());
            data.settings().forEach(object::put);
            final List<RocketActionSettings> actionsOriginal = data.actions();
            if (!actionsOriginal.isEmpty()) {
                List<Map<String, Object>> actionsForWrite = new ArrayList<>();
                recursiveSettings(actionsOriginal, actionsForWrite);
                object.put(ACTIONS, actionsForWrite);
            }

            actions.add(object);
        }
    }

    private RocketActionSettings createAction(LinkedHashMap<String, Object> action) throws RocketActionSettingsRepositoryException {
        return QuickActionFactory.create(
                getOrGenerateId(action),
                action.get(TYPE).toString(),
                action
        );
    }

    private static String getOrGenerateId(LinkedHashMap<String, Object> action) {
        Object idAsObject = action.get(ID);
        if (idAsObject == null || "".equals(idAsObject.toString())) {
            idAsObject = UUID.randomUUID().toString();
        }
        return idAsObject.toString();
    }

    private static class QuickActionFactory {
        static RocketActionSettings createAction(LinkedHashMap<String, Object> action) throws RocketActionSettingsRepositoryException {
            return QuickActionFactory.create(
                    getOrGenerateId(action),
                    action.get(TYPE).toString(),
                    action
            );
        }

        static RocketActionSettings create(String id, String actionType, LinkedHashMap<String, Object> action) throws RocketActionSettingsRepositoryException {
            ArrayList<LinkedHashMap<String, Object>> actions =
                    (ArrayList<LinkedHashMap<String, Object>>) action.get(ACTIONS);
            action.remove(TYPE);
            action.remove(ID);
            action.remove(ACTIONS);

            if (actions == null || actions.isEmpty()) {
                Map<String, String> map = new TreeMap<>();
                action.forEach((k, v) -> map.put(k, v == null ? "" : v.toString()));

                return new MutableRocketActionSettings(id, actionType, map, Collections.emptyList());
            } else {
                List<RocketActionSettings> settings = new ArrayList<>();
                for (LinkedHashMap<String, Object> a : actions) {
                    settings.add(createAction(a));
                }
                Map<String, String> map = new TreeMap<>();
                action.forEach((k, v) -> map.put(k, v.toString()));
                return new MutableRocketActionSettings(id, actionType, map, settings);
            }
        }
    }
}
