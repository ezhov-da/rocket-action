package ru.ezhov.rocket.action.caching;

public class CacheFactory {
    private static Cache cache;

    public static Cache getInstance() {
        if (cache == null) {
            cache = new DiskCache();
        }

        return cache;
    }
}
