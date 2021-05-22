package ru.ezhov.rocket.action.icon;

public class IconRepositoryFactory {
    private static IconRepository repository;

    public static IconRepository getInstance() {
        if (repository == null) {
            repository = new ResourceIconRepository();
        }
        return repository;
    }
}
