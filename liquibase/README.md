# Скрипты наката БД

Запустить из корня проекта задачу Gradle модуля `application-core`:

```shell
.\gradlew.bat :application-core:liquibaseUpdate
```

## PROD

```shell
.\gradlew.bat :application-core:liquibaseUpdate "-Pliquibase.contexts=!dev"
```

## DEV

```shell
.\gradlew.bat :application-core:liquibaseUpdate -Pliquibase.contexts=dev
```
