package ru.ezhov.rocket.action.template.infrastructure;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import ru.ezhov.rocket.action.template.domain.Engine;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VelocityEngineImpl implements Engine {
    private static final Logger LOG = Logger.getLogger(VelocityEngineImpl.class.getName());

    @Override
    public String apply(String template, Map<String, String> values) {
        // Initialize the engine.
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
        engine.setProperty(Velocity.RESOURCE_LOADER, "string");
        engine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        engine.addProperty("string.resource.loader.repository.static", "false");
        engine.init();

        // Initialize my template repository. You can replace the "Hello $w" with your String.
        StringResourceRepository repo = (StringResourceRepository) engine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
        repo.putStringResource("template", template);

        // Set parameters for my template.
        VelocityContext context = new VelocityContext();
        values.forEach(context::put);

        // Get and merge the template with my parameters.
        Template templateVelocity = engine.getTemplate("template", "UTF-8");
        StringWriter writer = new StringWriter();
        templateVelocity.merge(context, writer);

        return writer.toString();
    }

    public List<String> words(String text) {
        List<String> words = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\$\\w+");
        Matcher matcher = pattern.matcher(text.replace("\n\r", ""));
        while (matcher.find()) {
            String w = matcher.group();
            if (!words.contains(w)) {
                words.add(matcher.group());
            }
        }
        return words;
    }
}
