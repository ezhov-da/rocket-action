package ru.ezhov.rocket.action.template.domain;

import java.util.List;
import java.util.Map;

public interface Engine {

    String apply(String template, Map<String, String> values);

    List<String> words(String text);
}
