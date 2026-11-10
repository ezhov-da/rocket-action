# AGENTS.md

Настольное Swing-приложение (Kotlin/Java 17), запускающее настраиваемые пользователем действия из YAML. Многомодульный Gradle-проект; UI является хостом плагинов.

## Сборка и запуск

- Используйте Gradle wrapper: `.\gradlew.bat` (Windows) или `./gradlew`. В этом рабочем окружении `gradle` отсутствует в PATH.
- Требуется JDK 17. Gradle 7.6.4 не поддерживает JDK 18+, а Kotlin 1.8.10 падает на JDK 25 (`IllegalArgumentException: 25.0.4.1`). В этом окружении сейчас есть только JDK 25 и некорректный `JAVA_HOME` по умолчанию, поэтому перед сборкой укажите JDK 17 в `JAVA_HOME`, например:
  `$env:JAVA_HOME="D:\allinone\tools\rocket-action\java\17.0.4"`.
- Полная сборка: `.\gradlew.bat clean build`. Jar приложения собирается Spring Boot-плагином (`bootJar`): `application-ui-swing/build/libs/rocket-action.jar`; плагины: `plugins/<name>/build/libs/rocket-action-plugin-<name>-fat.jar` (а также обычный `plugins/<name>/build/libs/*.jar`).
- Сборка только приложения (с зависимостями): `.\gradlew.bat :application-ui-swing:assemble` (или `:application-ui-swing:bootJar`).
- Не заменяйте `bootJar` обычным «толстым» jar: при ручном слиянии JAR-ов от каждого пути остаётся один файл, поэтому теряются `META-INF/spring.factories` и `AutoConfiguration.imports` из части библиотек (приложение стартует без веб-сервера и без `application.properties`). `bootJar` хранит зависимости вложенными JAR-ами, и метаданные каждой библиотеки сохраняются. По этой же причине задача `fatJar` для `application-ui-swing` отключена.
- Отдельный плагин: `.\gradlew.bat :plugins:note:fatJar`, затем скопируйте `plugins/note/build/libs/rocket-action-plugin-note-fat.jar` в `plugin-jars/`.
- Запуск: `java -jar application-ui-swing/build/libs/rocket-action.jar [actions.yml]`. Первый аргумент CLI — путь к файлу действий (по умолчанию `./actions.yml`).
- Запуск из Gradle без сборки fat jar: `.\gradlew.bat :application-ui-swing:run`. Задача использует корень проекта как рабочую директорию и принимает аргументы через `--args`.
- Режим разработки (`.run/app-dev.run.xml`) игнорирует аргумент CLI и загружает `test-actions.yml` из classpath:
  `-Drocket.action.is.developer=true -Drocket.action.jar.plugin.folder=plugin-jars -Drocket.action.handler.server.port=4566 -Drocket.action.chain-action.enabled=true`
- `copy-plugins-dev.sh` наполняет игнорируемый Git каталог `plugin-jars/` из всех `plugins/*/build/libs/*-fat.jar`.
- `plugin-jars/`, `cache/`, `notes/` и `.jira-plugin/` — локальные runtime-данные; не коммитьте их содержимое. Каталог `plugins/` теперь содержит Gradle-модули плагинов.
- Релиз: измените версию приложения в `moduleVersions` корневого `build.gradle.kts` (ключи — пути Gradle-проектов; для плагинов — `${plugin-api.version}_N`), выполните `clean build`, затем `copy-app-to-prod.sh` / `copy-all-to-prod.sh` (они используют `$ROCKET_ACTION`).

## Структура Gradle-проекта

- `settings.gradle.kts` — список модулей (порядок как в Maven). Все плагины — подпроекты агрегирующего модуля `plugins` (например, `:plugins:note` в каталоге `plugins/note`).
- `build.gradle.kts` (корень) — общие соглашения: Java 17, Kotlin 1.8.10, версии и artifactId модулей, фильтрация ресурсов, задача `fatJar`, принудительные версии зависимостей.
- `gradle/libs.versions.toml` — централизованные версии библиотек (перенесены из Maven parent POM и модульных POM).
- `build.gradle.kts` каждого модуля — зависимости, соответствующие scope из Maven (`compile` → `implementation`, `provided` → `compileOnly`, `test` → `testImplementation`).
- `fatJar` собирает JAR с классами модуля и зависимостями `runtimeClasspath`; имена JAR повторяют Maven assembly (`*-fat.jar`). Для плагинов это основной артефакт; приложение вместо этого пакуется Spring Boot-плагином в `bootJar` (`rocket-action.jar`, зависимости — вложенные JAR-ы).

## Тесты

- Линтер, форматтер и typecheck не настроены.
- `.\gradlew.bat test` запускает JUnit 5 через `useJUnitPlatform()` (в отличие от старого Maven surefire, тесты действительно выполняются). Для одного класса: `.\gradlew.bat :application-ui-swing:test --tests "*ClassName*"`.
- Многие файлы `application-ui-swing/src/test/.../*Test.kt` — это ручные Swing-харнессы с `main()`, а не JUnit. Gradle их не запускает; запускайте из IDE. `.run/*.run.xml` покрывает харнессы загрузчика плагинов.
- Тесты `application-core` используют H2 + Liquibase; Gradle по умолчанию делает рабочей директорией каталог модуля, чего и ожидают тесты. Changelog читается через `CompositeResourceAccessor` (`FileSystemResourceAccessor` + `ClassLoaderResourceAccessor`), поэтому XSD `dbchangelog-*.xsd` берётся из `liquibase-core`, а не скачивается с liquibase.org — тесты проходят без сети.
- `BuildIconsTest` — генератор, а не тест: он перезаписывает `application-ui-swing/src/main/resources/icons/icons.json`. Задача `:application-ui-swing:test` исключает его; запускайте вручную из IDE и проверяйте `git diff`.
- `ru.ezhov.rocket.action.webtest.WebServerIntegrationTest` поднимает минимальное Spring Boot web-приложение на случайном порту (`--server.port=0`) и проверяет редирект `/` → Swagger UI, `/swagger.json`, фильтр API-ключей и MCP-сервлет. Конфигурацию для boot-тестов держите в отдельном пакете вне `ru.ezhov.rocket.action.application`: тестовые `@Configuration` из этого пакета подхватываются сканированием контекста, который создаёт `ApplicationContextFactory.context()`, и ломают остальные тесты (`BeanDefinitionOverrideException`).
- Запуск приложения вне режима разработки записывает данные в отслеживаемые файлы состояния в `.rocket-action/` (`variables.json`, `api-keys.json`, `configurations.json`, `chain-actions.json`, `atomic-actions.json`) и создаёт `actions.yml`; не коммитьте случайные изменения.

## Архитектура

- `application-ui-swing` — реальное приложение. Точка входа `ru.ezhov.rocket.action.application.AppKt`: сначала инициализируется AWT (FlatLaf и Swing), затем запускается Spring Boot-приложение `RocketActionSpringBootApplication` (`ApplicationContextFactory.start()`), которое сканирует компоненты в `ru.ezhov.rocket.action`.
- Собственный core-код приложения находится в `application-ui-swing/.../application/core`; он не связан с Gradle-модулем `application-core`.
- `application-core` — отдельное хранилище действий на H2/Ktorm. Swing-приложение **не зависит** от него (действия сохраняются в YAML через `YmlRocketActionSettingsRepository`); изменения там не повлияют на работающее приложение.
- `plugins/api` (`:plugins:api`) — контракт плагинов. Модули плагинов зависят от него (и от используемых общих библиотек) через `compileOnly` и не должны включать его в сборку.
- `plugins/template` (`:plugins:template`) — модуль-шаблон для новых плагинов. Новые модули создавайте в каталоге `plugins/` и добавляйте в `settings.gradle.kts` и в `moduleVersions` корневого `build.gradle.kts`.
- Обнаружение плагинов: JAR-файлы в `plugins/` (или в каталоге из `rocket.action.jar.plugin.folder`), FQCN по одному в строке в `META-INF/rocket-action-plugins.properties`; Groovy-скрипты в `groovy-plugins/*.groovy` должны возвращать `RocketActionPlugin`. Встроенные плагины GROUP и CHAIN_ACTION находятся в `InnerPluginLoader`.
- UI должен создаваться вне Spring: FlatLaf инициализируется до контекста Spring (`App.kt`); не переносите создание UI в бины.

## Соглашения по плагинам, зависимостям и сборке

- Версии модулей задаются в `moduleVersions` корневого `build.gradle.kts` по путям Gradle-проектов (`:plugins:api`, `:plugins:note`, ...); версия `plugin-api` используется в версиях плагинов (`${plugin-api.version}_N`). При изменении синхронизируйте `plugins/api` и зависимые плагины.
- Версии библиотек меняйте в `gradle/libs.versions.toml`. Не добавляйте версии в модульные build-скрипты без необходимости.
- Соглашение о метаданных плагина: `src/main/resources/config/plugin-<name>.properties` с `version`/`author`/`link`; `${project.version}` подставляется фильтрацией ресурсов Gradle (Ant `ReplaceTokens` для `**/*.properties`).
- Код должен оставаться совместимым с Java 17 (`sourceCompatibility`/`targetCompatibility` 17, `JavaCompile.options.release=17`, Kotlin `jvmTarget=17` и `-Xjdk-release=17`).
- `application-ui-swing` использует Kotlin-плагин компилятора `spring` (all-open) — он необходим для смешанной сборки Kotlin/Java и Spring-компонентов.
- Версии Kotlin runtime и SLF4J/Logback принудительно зафиксированы в корневом `build.gradle.kts`, потому что Gradle выбирает наибольшую версию, а Maven — ближайшую. Jackson в `application-ui-swing` зафиксирован отдельно (MCP требует Jackson 2.21, несовместимый с Kotlin 1.8).
- Плагины объявляют `kotlin-stdlib` как `compileOnly`; `kotlin-stdlib` исключён из `runtimeClasspath` плагинов, чтобы `*-fat.jar` не содержал стандартную библиотеку (как в Maven `provided`).
- Посмотреть фактическое дерево зависимостей: `.\gradlew.bat :<module>:dependencies --configuration runtimeClasspath`.

## Конфигурация и runtime

- Все настройки задаются через `-Drocket.action.*`; канонический список и документация — в `UsedPropertiesName.kt`; значения по умолчанию — в `application-ui-swing/src/main/resources/general.properties`.
- Логирование: `-DLOGGER_NAME=ru.ezhov -DLOGBACK_LOGGER_LEVEL=DEBUG` (см. раздел «Logging» в `README.md`).
- HTTP-сервер: приложение является обычным Spring Boot-приложением (Spring Boot 3.2 / Spring 6.1 / Tomcat 10.1 / `jakarta.servlet`) и стартует всегда вместе с UI; порт по умолчанию 4567 задаётся в `application-ui-swing/src/main/resources/application.properties` (`server.port=${rocket.action.handler.server.port}`), переопределяется через `-Drocket.action.handler.server.port=...`. Эндпоинты: `GET/POST /api/v1/handlers/{id}/{command}` (`@RestController`), `POST /mcp` (отдельный сервлет MCP из `McpServerConfig`), `GET /swagger.json`; UI Swagger доступен по `/` и `/swagger-ui/`. Заголовок авторизации `X-Rocket-Action-Handler-Key` (ключи в `.rocket-action/api-keys.json`), проверяется фильтром `ApiKeyFilter` для обоих путей. Примеры в `handlers.http` используют другой порт; ориентируйтесь на свойство.
- Spring Boot запускается в headless-режиме (`spring.main.headless=true`); Swing/AWT инициализируется до создания контекста Boot (`App.kt`). `ApplicationContextFactory.context()` для тестов создаёт non-web контекст и принудительно ставит `java.awt.headless=false`.
- Liquibase (только для H2-хранилища `application-core`): выполните `.\gradlew.bat :application-core:liquibaseUpdate -Pliquibase.contexts=dev` (или `"-Pliquibase.contexts=!dev"` для prod). Это обновляет отслеживаемый `db/test/rocket-action/rocket-action.mv.db`.
