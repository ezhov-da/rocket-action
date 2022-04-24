# Скрипты наката БД

Запустить в модуле `application-core` команду `mvn liquibase:update`.

Запуск в модуле `application-core` через командную строку

## PROD

```shell
mvn liquibase:update -Dliquibase.contexts='!dev'
```

## DEV

```shell
mvn liquibase:update -Dliquibase.contexts='dev'
```
