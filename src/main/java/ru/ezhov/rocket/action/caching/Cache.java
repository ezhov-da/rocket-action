package ru.ezhov.rocket.action.caching;

import java.io.File;
import java.net.URL;
import java.util.Optional;

public interface Cache {
    Optional<File> fromCache(URL url);
}
