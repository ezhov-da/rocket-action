package ru.ezhov.rocket.action.caching;

import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class DiskCacheTest {

    @Test
    public void test() throws MalformedURLException {
        DiskCache cache = new DiskCache();
        final Optional<File> fileOptional = cache.fromCache(new URL("https://www.elastic.co/favicon.ico"));
        if (fileOptional.isPresent()) {
            System.out.println(fileOptional);
        }
    }
}