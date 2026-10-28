# AGENTS.md

Настольное Swing-приложение (Kotlin/Java 8), запускающее настраиваемые пользователем действия из YAML. Многомодульный Maven-проект; UI является хостом плагинов.

## Сборка и запуск

- Используйте Maven wrapper: `.\mvnw.cmd` (Windows) или `./mvnw`. В этом рабочем окружении `mvn` отсутствует в PATH.
- Требуется JDK 8–17. Kotlin 1.8.10 падает на JDK 25 (`IllegalArgumentException: 25.0.4.1`). В этом окружении сейчас есть только JDK 25 и некорректный `JAVA_HOME` по умолчанию, поэтому перед сборкой укажите поддерживаемый JDK в `JAVA_HOME`.
- Полная сборка: `.\mvnw.cmd clean package`. Jar приложения: `application-ui-swing/target/rocket-action.jar`; плагины: `<module>/target/*-fat.jar`.
- Сборка только приложения (с зависимостями): `.\mvnw.cmd -pl application-ui-swing -am package -DskipTests`.
- Отдельный плагин: `.\mvnw.cmd -pl plugin-note -am package -DskipTests`, затем скопируйте `plugin-note/target/*-fat.jar` в `plugin-jars/`.
- Запуск: `java -jar application-ui-swing/target/rocket-action.jar [actions.yml]`. Первый аргумент CLI — путь к файлу действий (по умолчанию `./actions.yml`).
- Режим разработки (`.run/app-dev.run.xml`) игнорирует аргумент CLI и загружает `test-actions.yml` из classpath:
  `-Drocket.action.is.developer=true -Drocket.action.jar.plugin.folder=plugin-jars -Drocket.action.handler.server.port=4566 -Drocket.action.chain-action.enabled=true`
- `copy-plugins-dev.sh` наполняет игнорируемый Git каталог `plugin-jars/` из всех `plugin-*/target/*-fat.jar`.
- `plugins/`, `plugin-jars/`, `cache/`, `notes/` и `.jira-plugin/` — локальные runtime-данные; не коммитьте их содержимое.
- Релиз: измените версию в `application-ui-swing/pom.xml`, выполните `clean package`, затем `copy-app-to-prod.sh` / `copy-all-to-prod.sh` (они используют `$ROCKET_ACTION`).

## Тесты

- Линтер, форматтер и typecheck не настроены.
- `mvn test` запускает **ноль тестов JUnit 5**: surefire-плагин не зафиксирован, поэтому стандартный `maven-surefire-plugin:2.12.4` в Maven 3.8.4 не видит JUnit 5 (в существующих отчётах `Tests run: 0`). Запускайте JUnit-тесты из IDE или явно вызывайте современный surefire.
- Один JUnit-тест без правки POM: `.\mvnw.cmd -pl application-ui-swing -am install -DskipTests`, затем `.\mvnw.cmd -pl application-ui-swing org.apache.maven.plugins:maven-surefire-plugin:3.2.5:test -Dtest=ClassName#method`.
- Многие файлы `application-ui-swing/src/test/.../*Test.kt` — это ручные Swing-харнессы с `main()`, а не JUnit. Запускайте их из IDE; `.run/*.run.xml` покрывает харнессы загрузчика плагинов.
- Тесты `application-core` используют H2 + Liquibase и ищут changelog по пути `./../liquibase`; рабочей директорией должен быть `application-core` (Maven делает это по умолчанию; в IDE задайте явно).
- `BuildIconsTest` перезаписывает отслеживаемый `application-ui-swing/src/main/resources/icons/icons.json` при запуске; после этого проверяйте `git diff`.
- Запуск приложения вне режима разработки записывает данные в отслеживаемые файлы состояния в `.rocket-action/` (`variables.json`, `api-keys.json`, `configurations.json`, `chain-actions.json`, `atomic-actions.json`) и создаёт `actions.yml`; не коммитьте случайные изменения.

## Архитектура

- `application-ui-swing` — реальное приложение. Точка входа `ru.ezhov.rocket.action.application.AppKt`; Spring 5 сканирует компоненты в `ru.ezhov.rocket.action` (`AppConfigCoreSpring`, `lazyInit = false`).
- Собственный core-код приложения находится в `application-ui-swing/.../application/core`; он не связан с Maven-модулем `application-core`.
- `application-core` — отдельное хранилище действий на H2/Ktorm. Swing-приложение **не зависит** от него (действия сохраняются в YAML через `YmlRocketActionSettingsRepository`); изменения там не повлияют на работающее приложение.
- `plugin-api` — контракт плагинов. POM-файлы плагинов зависят от него (и от используемых общих библиотек) с `<scope>provided</scope>` и не должны включать его в сборку.
- `plugin-template` — модуль-шаблон для новых плагинов. Новые модули добавляйте в `<modules>` корневого `pom.xml`.
- Обнаружение плагинов: JAR-файлы в `plugins/` (или в каталоге из `rocket.action.jar.plugin.folder`), FQCN по одному в строке в `META-INF/rocket-action-plugins.properties`; Groovy-скрипты в `groovy-plugins/*.groovy` должны возвращать `RocketActionPlugin`. Встроенные плагины GROUP и CHAIN_ACTION находятся в `InnerPluginLoader`.
- UI должен создаваться вне Spring: FlatLaf инициализируется до контекста Spring (`App.kt`); не переносите создание UI в бины.

## Соглашения по плагинам и сборке

- POM-файлы плагинов используют версию `${plugin-api.version}_N`; версия `plugin-api` дублируется в `<version>` модуля и в `<properties>` корневого POM, поэтому синхронизируйте оба места (то же для `utils`/`ui-utils`).
- Соглашение о метаданных плагина: `src/main/resources/config/plugin-<name>.properties` с `version`/`author`/`link`; `${project.version}` подставляется Maven-фильтрацией.
- Код должен оставаться совместимым с Java 8 (например, поэтому Quartz зафиксирован на 2.4.0).
- В `application-ui-swing` не удаляйте Kotlin-плагин компилятора `spring` (all-open) и отключённые executions `default-compile`; от них зависит смешанная сборка Kotlin/Java.

## Конфигурация и runtime

- Все настройки задаются через `-Drocket.action.*`; канонический список и документация — в `UsedPropertiesName.kt`; значения по умолчанию — в `application-ui-swing/src/main/resources/general.properties`.
- Логирование: `-DLOGGER_NAME=ru.ezhov -DLOGBACK_LOGGER_LEVEL=DEBUG` (см. `README-DEV.md`).
- HTTP-сервер: порт по умолчанию 4567 (`rocket.action.handler.server.port`); `GET/POST /api/v1/handlers/{id}/{command}`, `POST /mcp`, `GET /swagger.json`; заголовок авторизации `X-Rocket-Action-Handler-Key` (ключи в `.rocket-action/api-keys.json`). Примеры в `handlers.http` используют другой порт; ориентируйтесь на свойство.
- Liquibase (только для H2-хранилища `application-core`): из `application-core` выполните `..\mvnw.cmd liquibase:update -Dliquibase.contexts=dev` (или `"-Dliquibase.contexts=!dev"` для prod). Это обновляет отслеживаемый `db/test/rocket-action/rocket-action.mv.db`.
