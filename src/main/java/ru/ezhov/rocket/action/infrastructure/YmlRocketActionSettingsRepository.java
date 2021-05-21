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

public class YmlRocketActionSettingsRepository implements RocketActionSettingsRepository {

    private final URI uri;

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
            throw new RocketActionSettingsRepositoryException("//TODO", ex);
        }
    }

    @Override
    public void save(List<RocketActionSettings> settings) throws RocketActionSettingsRepositoryException {
        File file = new File(uri.getPath());
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            Yaml yaml = new Yaml();

            List<Map<String, Object>> all = new ArrayList<>();
            recursiveSettings(settings, all);

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("actions", all);
            yaml.dump(map, outputStreamWriter);
        } catch (Exception ex) {
            throw new RocketActionSettingsRepositoryException("//TODO", ex);
        }
    }

    private void recursiveSettings(List<RocketActionSettings> settings, List<Map<String, Object>> actions) {
        for (RocketActionSettings data : settings) {
            Map<String, Object> object = new LinkedHashMap<>();
            object.put("type", data.type());
            data.settings().forEach(object::put);
            final List<RocketActionSettings> actionsOriginal = data.actions();
            if (!actionsOriginal.isEmpty()) {
                List<Map<String, Object>> actionsForWrite = new ArrayList<>();
                recursiveSettings(actionsOriginal, actionsForWrite);
                object.put("actions", actionsForWrite);
            }

            actions.add(object);
        }
    }

    private RocketActionSettings createAction(LinkedHashMap<String, Object> action) throws RocketActionSettingsRepositoryException {
        String typeAsString = action.get("type").toString();
        return QuickActionFactory.create(typeAsString, action);
    }

    private static class QuickActionFactory {
        static RocketActionSettings createAction(LinkedHashMap<String, Object> action) throws RocketActionSettingsRepositoryException {
            String typeAsString = action.get("type").toString();
            return QuickActionFactory.create(typeAsString, action);
        }

        static RocketActionSettings create(String actionType, LinkedHashMap<String, Object> action) throws RocketActionSettingsRepositoryException {
            ArrayList<LinkedHashMap<String, Object>> actions =
                    (ArrayList<LinkedHashMap<String, Object>>) action.get("actions");
            action.remove("type");
            action.remove("actions");

            if (actions == null || actions.isEmpty()) {
                Map<String, String> map = new TreeMap<>();
                action.forEach((k, v) -> map.put(k, v.toString()));

                return new MutableRocketActionSettings(actionType, map, Collections.emptyList());
            } else {
                List<RocketActionSettings> settings = new ArrayList<>();
                for (LinkedHashMap<String, Object> a : actions) {
                    settings.add(createAction(a));
                }
                Map<String, String> map = new TreeMap<>();
                action.forEach((k, v) -> map.put(k, v.toString()));
                return new MutableRocketActionSettings(actionType, map, settings);
            }
        }
    }
}
