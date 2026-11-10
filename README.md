# rocket-action

[![Java 17](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)](https://adoptium.net/temurin/releases/?version=17)
[![Kotlin 1.8](https://img.shields.io/badge/Kotlin-1.8-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Gradle 7.6](https://img.shields.io/badge/Gradle-7.6-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**A desktop launcher for your personal actions, configured in YAML.**

rocket-action is a Java 17 Swing application that turns a YAML file into a searchable
menu of actions. An action can display text or images, open a URL or a file, run a
command or a Groovy script, create a note, call Todoist or Jira, or execute a chain
of several steps. The application is also a plugin host: bundled and third-party
plugins add new action types without changing the core.

- [Features](#features)
- [Requirements](#requirements)
- [Quick start](#quick-start)
- [Configuring actions](#configuring-actions)
- [Action types](#action-types)
- [Plugins](#plugins)
- [Configuration](#configuration)
- [HTTP API and MCP](#http-api-and-mcp)
- [Project layout](#project-layout)
- [Development](#development)
- [Contributing](#contributing)
- [Acknowledgements](#acknowledgements)
- [License](#license)

## Features

- **YAML-driven** — actions are plain YAML, easy to read, edit, version and share.
- **Fast search** — find any action by label or description and run it from the keyboard.
- **Rich action types** — text, images and SVG, URLs with placeholders and history,
  files, clipboard, shell commands, Groovy scripts, notes, Todoist, Jira, GitHub Gists,
  URL parsing and temporary files.
- **Groups and chains** — combine actions into nested groups and chain actions with
  variables; chains can also be scheduled.
- **Plugin architecture** — new action types can be delivered as JAR or Groovy
  plugins; the plugin API is a separate, stable contract.
- **HTTP API** — every action can be exposed as a handler and called remotely; an
  OpenAPI/Swagger UI is available.
- **MCP server** — actions can be used from MCP-compatible AI clients.
- **Themes** — FlatLaf look-and-feel with light, dark, IntelliJ and Darcula variants.
- **Local-first** — notes, variables, configuration and API keys are stored on disk;
  no account is required.

## Requirements

- **JDK 17**. Gradle 7.6.4 does not support JDK 18 or newer, and Kotlin 1.8.10 fails
  on JDK 25 with `IllegalArgumentException: 25.0.4.1`.
- Windows, macOS or Linux with a desktop environment (the UI is Swing/AWT).
- An internet connection is only needed for actions that fetch remote data.

## Quick start

### Build from source

Clone the repository and build with the Gradle wrapper. Set `JAVA_HOME` to a JDK 17
installation first:

```powershell
# Windows
$env:JAVA_HOME = "C:\path\to\jdk-17"
.\gradlew.bat clean build
```

```bash
# Linux / macOS
export JAVA_HOME=/path/to/jdk-17
./gradlew clean build
```

The build produces:

- `application-ui-swing/build/libs/rocket-action.jar` — the application (Spring Boot
  executable JAR, dependencies are nested inside);
- `plugins/<name>/build/libs/rocket-action-plugin-<name>-fat.jar` — plugin JARs.

### Run

The first command-line argument is the path to the actions file. It defaults to
`./actions.yml`, which is created automatically if it does not exist.

```bash
java -jar application-ui-swing/build/libs/rocket-action.jar
java -jar application-ui-swing/build/libs/rocket-action.jar /path/to/my-actions.yml
```

Plugin JARs are loaded from the `plugins` folder by default. For development, copy the
built plugins into `plugin-jars` and point the application there:

```bash
./copy-plugins-dev.sh
java -Drocket.action.jar.plugin.folder=plugin-jars \
  -jar application-ui-swing/build/libs/rocket-action.jar
```

On Windows, copy `plugins\*\build\libs\*-fat.jar` into `plugin-jars\` manually or from
PowerShell, then pass the same `-D` option.

### Windows launcher scripts

`rocket-action.jar` is a Spring Boot executable JAR whose manifest uses `JarLauncher`.
That launcher ignores `-Dloader.path` and `-Dloader.main`, so external plugin JARs are
not added to the classpath. If the deployment keeps the `script` plugin and its
libraries outside the bundled application (for example in `plugins-ext` and `lib-ext`),
launch the application through `PropertiesLauncher` instead of `-jar`.

Save this as `run.bat` next to `rocket-action.jar` for a normal run:

```bat
@echo off
chcp 65001 >nul
cd /d %~dp0

java\17.0.4\bin\java ^
 -Dfile.encoding=UTF-8 ^
 -DLOGGER_NAME=ru.ezhov ^
 -DLOGBACK_LOGGER_LEVEL=INFO ^
 -Dloader.path=plugins-ext,lib-ext ^
 -Dloader.main=ru.ezhov.rocket.action.application.AppKt ^
 -Drocket.action.extended.plugin.classess=ru.ezhov.rocket.action.plugin.script.groovy.ui.GroovyScriptRocketActionUi;ru.ezhov.rocket.action.plugin.script.dynamic.ui.DynamicScriptRocketActionUi ^
 -Drocket.action.chain-action.enabled=true ^
 -Drocket.action.ui.configuration.look_and_feel.class=com.formdev.flatlaf.FlatIntelliJLaf ^
 -Xmx1024m ^
 -cp rocket-action.jar ^
 org.springframework.boot.loader.launch.PropertiesLauncher ^
 ".rocket-action\actions.yml"
```

For troubleshooting, `run-debug.bat` enables DEBUG logging for the `ru.ezhov` logger:

```bat
@echo off
chcp 65001 >nul
cd /d %~dp0

java\17.0.4\bin\java ^
 -Dfile.encoding=UTF-8 ^
 -DLOGGER_NAME=ru.ezhov ^
 -DLOGBACK_LOGGER_LEVEL=DEBUG ^
 -Dloader.path=plugins-ext,lib-ext ^
 -Dloader.main=ru.ezhov.rocket.action.application.AppKt ^
 -Drocket.action.extended.plugin.classess=ru.ezhov.rocket.action.plugin.script.groovy.ui.GroovyScriptRocketActionUi;ru.ezhov.rocket.action.plugin.script.dynamic.ui.DynamicScriptRocketActionUi ^
 -Drocket.action.chain-action.enabled=true ^
 -Drocket.action.ui.configuration.look_and_feel.class=com.formdev.flatlaf.FlatIntelliJLaf ^
 -Xmx1024m ^
 -cp rocket-action.jar ^
 org.springframework.boot.loader.launch.PropertiesLauncher ^
 ".rocket-action\actions.yml"
```

Notes:

- `loader.path` is a comma-separated list, so use `plugins-ext,lib-ext`; do not use `;`
  or `\*` there.
- `rocket.action.extended.plugin.classess` is a separate `;`-separated list.
- `cd /d %~dp0` makes all paths relative to the script, so
  `.rocket-action\actions.yml` is resolved inside the deployment directory. The file is
  created empty if it does not exist, so make sure the real actions file is in place.
- To attach a remote Java debugger, add
  `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005` to
  `run-debug.bat`.
- If all plugin JARs are in one folder and are loaded by the built-in plugin loader,
  the simpler form can be used instead:
  `java -Drocket.action.jar.plugin.folder=plugins -jar rocket-action.jar`.

### Run from Gradle

```powershell
.\gradlew.bat :application-ui-swing:run
```

The task uses the project root as the working directory and accepts arguments via
`--args`, for example `--args="my-actions.yml"`.

## Configuring actions

Actions live in a YAML file:

```yaml
---
actions:
  - type: GROUP
    _id: 7f1c2d3e-4a5b-4c6d-8e9f-0a1b2c3d4e5f
    label: Work
    description: Daily work links
    actions:
      - type: OPEN_URL_WITH_TEXT
        _id: 1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d
        label: Open issue _____
        description: Open a Jira issue by key
        baseUrl: https://jira.example.com/browse/_____
        placeholder: _____

      - type: SEPARATOR
        _id: 2b3c4d5e-6f7a-4b8c-9d0e-1f2a3b4c5d6e

  - type: SHOW_TEXT
    _id: 3c4d5e6f-7a8b-4c9d-0e1f-2a3b4c5d6e7f
    label: |
      Any text that should always be at hand.
```

Common fields:

| Field         | Required | Description                                              |
|---------------|----------|----------------------------------------------------------|
| `type`        | yes      | Action type, for example `SHOW_TEXT` or `OPEN_URL`.      |
| `_id`         | yes      | Stable unique identifier (UUID).                         |
| `label`       | usually  | Text shown in the menu and used by search.               |
| `description` | no       | Tooltip and additional search text.                      |
| `iconUrl`     | no       | URL or file path of the action icon.                     |

Every action type adds its own fields — see [Action types](#action-types) and the
plugin documentation.

## Action types

### Built-in

Built-in types are part of the application and need no plugin JAR.

| Type                      | Description                                                        |
|---------------------------|--------------------------------------------------------------------|
| `GROUP`                   | Groups actions into a submenu.                                     |
| `CHAIN_OR_ATOMIC_ACTION`  | Runs chain and atomic actions. Enable with `rocket.action.chain-action.enabled=true`. |

### Bundled plugins

The repository ships the following plugins. Build them and place the `*-fat.jar` files
in the plugin folder.

| Plugin             | Action type(s)                                              | Description                                              |
|--------------------|-------------------------------------------------------------|----------------------------------------------------------|
| `copy-to-clipboard`| `COPY_TO_CLIPBOARD`                                         | Copies configured text to the clipboard.                 |
| `exec`             | `EXEC`                                                      | Runs an external command.                                |
| `gist`             | `GIST`                                                      | Lists GitHub Gists of a user.                            |
| `jira`             | `JIRA_WORK_LOG`                                             | Logs work time to a Jira issue.                          |
| `note`             | `NOTE`                                                      | Creates and stores notes (SQLite).                       |
| `note-on-file`     | `NOTE_ON_FILE`                                              | Keeps a note attached to a file.                         |
| `open-file`        | `OPEN_FILE`                                                 | Opens a file with the system application.                |
| `open-url`         | `OPEN_URL`, `OPEN_URL_WITH_TEXT`, `OPEN_URL_WITH_TEXT_HISTORY` | Opens a URL, optionally asking for a value or keeping input history. |
| `script`           | `GROOVY_SCRIPT`, `DYNAMIC_SCRIPT`                           | Runs Groovy scripts and dynamic scripts.                 |
| `separator`        | `SEPARATOR`                                                 | Visual separator in the menu.                            |
| `show-image`       | `SHOW_IMAGE`                                                | Shows an image from a URL or a file.                     |
| `show-image-svg`   | `SHOW_SVG_IMAGE`                                            | Shows an SVG image from a URL or a file.                 |
| `temporary-file`   | `SAVE_TEMPORARY_FILE`                                       | Saves text to a temporary file.                          |
| `text`             | `SHOW_TEXT`, `SHOW_TEXT_AS_MENU`                            | Shows text as a panel or as a submenu.                   |
| `todoist`          | `TODOIST`                                                   | Creates Todoist tasks and projects.                      |
| `url-parser`       | `URL_PARSER`                                                | Parses URL parts and copies them.                        |
| `template`         | `COPY_TO_CLIPBOARD_TEMPLATE`                                | Example plugin: copies text rendered from a template.    |

## Startup actions

Rocket Action can run an ordered list of AtomicAction and ChainAction entries right after
the main window appears. The list is managed in a separate **Startup** window
(**Chains → Startup**).

| Element | Default | Description |
|---|---|---|
| List file | `./.rocket-action/startup-actions.json` | Ordered list of entries and the `stopOnError` flag. |
| Run log | `./.rocket-action/startup-actions/log.txt` | Append-only history of finished runs. |
| Run order | — | Array order in the JSON file. |
| `stopOnError` | `false` | Stop the run after the first execution failure. Missing actions never stop the run. |
| `input` | empty | Passed only to actions that accept input; for a chain the first AtomicAction decides. |
| `enabled` | `true` | Disabled entries are skipped. |
| `rocket.action.startup-actions.enabled` | `true` | Kill switch for automatic execution at start; `Run now` keeps working. |

`startup-actions.json`:

```json
{
  "stopOnError": false,
  "startupActions": [
    {
      "id": "entry-1",
      "actionId": "action-1",
      "input": null,
      "enabled": true
    }
  ]
}
```

Behavior details:

- The list runs in the background after the main window becomes visible; the application stays usable.
- A run executes a snapshot of the saved list. Edits apply to the next run.
- **Run now** executes only the saved list. When the editor has unsaved changes, the application offers `Save and run`.
- While a run is active, editing controls in the Startup window are disabled, but the list, the last-run
  results and the log stay readable. The rest of the application is not blocked.
- Only an execution failure counts as an error. An entry that references a deleted action is skipped
  with a warning, and the run continues.
- Deleting an AtomicAction or ChainAction removes its startup entries.
- The last-run result lives in memory and resets on restart; the log file keeps the history.

## Plugins

Plugins are discovered at startup from two locations:

| Location                        | Property                                | Default        |
|---------------------------------|-----------------------------------------|----------------|
| JAR plugins                     | `rocket.action.jar.plugin.folder`       | `plugins`      |
| Groovy scripts (`*.groovy`)     | `rocket.action.groovy.plugin.folder`    | `groovy-plugins` |

JAR plugins declare their entry points in
`META-INF/rocket-action-plugins.properties` (one fully qualified class name per line).
A Groovy script must return a `RocketActionPlugin` instance. See the
[`plugins/api`](plugins/api) module for the contract and [`plugins/template`](plugins/template)
for a minimal example.

To build a single plugin:

```powershell
.\gradlew.bat :plugins:note:fatJar
```

The result is `plugins/note/build/libs/rocket-action-plugin-note-fat.jar`.

Plugin modules use `compileOnly` for `plugins/api` and `kotlin-stdlib`: the application
provides them at runtime, so plugin fat JARs do not bundle them.

## Configuration

All settings are JVM system properties with the `rocket.action.` prefix. Defaults live in
`application-ui-swing/src/main/resources/general.properties`; the canonical documented
list is the `UsedPropertiesName` enum.

| Property                                                | Default                              | Description                                              |
|---------------------------------------------------------|--------------------------------------|----------------------------------------------------------|
| `rocket.action.default.actions.file`                    | `./actions.yml`                      | Actions file used when no CLI argument is given.         |
| `rocket.action.cache.folder`                            | `./cache`                            | Cache folder for downloaded files.                       |
| `rocket.action.jar.plugin.folder`                       | `plugins`                            | Folder with plugin JARs.                                 |
| `rocket.action.groovy.plugin.folder`                    | `groovy-plugins`                     | Folder with Groovy plugins.                              |
| `rocket.action.handler.server.port`                     | `4567`                               | Port of the embedded HTTP server.                        |
| `rocket.action.chain-action.enabled`                    | `false`                              | Enables chain and atomic actions.                        |
| `rocket.action.startup-actions.enabled`                 | `true`                               | Runs the startup actions list at application start.      |
| `rocket.action.startup-actions.file.repository.path`    | `./.rocket-action/startup-actions.json` | File with the ordered startup list.                   |
| `rocket.action.startup-actions.log.file.path`           | `./.rocket-action/startup-actions/log.txt` | Startup actions run log.                          |
| `rocket.action.ui.tags.isEnabled`                       | `false`                              | Enables tags in the UI.                                  |
| `rocket.action.ui.configuration.look_and_feel.class`    | `com.formdev.flatlaf.FlatLightLaf`   | FlatLaf look-and-feel class.                             |
| `rocket.action.ui.base-dialog.opacity`                  | `0.4`                                | Base dialog opacity, from `0.1` to `1`.                  |
| `rocket.action.ui.configuration.frame.always_on_top`    | `false`                              | Keeps the configuration window on top.                   |
| `rocket.action.is.developer`                            | `false`                              | Developer mode: loads `test-actions.yml` from the classpath. |

Example:

```bash
java -Drocket.action.default.actions.file=./my-actions.yml \
     -Drocket.action.handler.server.port=4566 \
     -jar application-ui-swing/build/libs/rocket-action.jar
```

### Logging

Logging is configured with Logback and controlled by two system properties:

```bash
java -DLOGGER_NAME=ru.ezhov -DLOGBACK_LOGGER_LEVEL=INFO \
  -jar application-ui-swing/build/libs/rocket-action.jar
```

### Runtime data

The application writes local state into `.rocket-action/` (`variables.json`,
`api-keys.json`, `configurations.json`, `chain-actions.json`, `atomic-actions.json`,
`startup-actions.json`, `startup-actions/log.txt`), and
uses `cache/`, `notes/` and `plugin-jars/` for runtime data. Do not commit these folders.

## HTTP API and MCP

The desktop application always starts an embedded Spring Boot server (port `4567` by
default):

| Endpoint                                  | Description                                      |
|-------------------------------------------|--------------------------------------------------|
| `GET /`                                   | Redirects to the Swagger UI.                     |
| `GET /swagger-ui/`                        | Swagger UI.                                      |
| `GET /swagger.json`                       | OpenAPI document with all available handlers.    |
| `GET/POST /api/v1/handlers/{id}/{command}` | Execute an action handler.                       |
| `POST /mcp`                               | MCP server endpoint.                             |

Requests are protected by the `X-Rocket-Action-Handler-Key` header. API keys are stored
in `.rocket-action/api-keys.json` and managed from the UI.

```bash
curl -X POST \
  -H "X-Rocket-Action-Handler-Key: <key>" \
  -H "Content-Type: application/json" \
  -d '{"text":"123"}' \
  http://localhost:4567/api/v1/handlers/<action-id>/openUrl
```

## Project layout

| Module                              | Purpose                                                              |
|-------------------------------------|----------------------------------------------------------------------|
| `application-ui-swing`              | The desktop application: Swing UI, Spring Boot host, HTTP API, MCP.  |
| `application-core`                  | Standalone H2/Ktorm action storage. **Not used by the desktop app.** |
| `plugins/api`                       | Plugin contract and extension points.                                |
| `plugins/*`                         | Bundled action plugins and the `template` example.                   |
| `ui-utils`, `utils`                 | Shared UI and utility code.                                          |
| `db`, `liquibase`                   | H2 schema and migrations for `application-core`.                     |

## Development

### Build and test

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
```

Run a single test class:

```powershell
.\gradlew.bat :application-ui-swing:test --tests "*ClassName*"
```

Some files under `application-ui-swing/src/test/` are manual Swing harnesses with a
`main()` method, not JUnit tests; run them from the IDE. `BuildIconsTest` is an icon
generator and is excluded from the `test` task.

Tests for `application-core` use H2 and Liquibase and expect the module directory as the
working directory (Gradle does this by default).

### Developer mode

Developer mode ignores the CLI argument and loads `test-actions.yml` from the classpath.
Pass the following VM options to the IDE run configuration:

```text
-Drocket.action.is.developer=true
-Drocket.action.jar.plugin.folder=plugin-jars
-Drocket.action.handler.server.port=4566
-Drocket.action.chain-action.enabled=true
```

`./copy-plugins-dev.sh` fills `plugin-jars/` with all built plugin fat JARs.

### Dependency tree

```bash
./gradlew :application-ui-swing:dependencies --configuration runtimeClasspath
```

### Release

1. Update the application version in `moduleVersions` in the root
   [`build.gradle.kts`](build.gradle.kts). Plugin versions use the
   `${plugin-api.version}_N` scheme.
2. Build everything:

   ```bash
   ./gradlew clean build
   ```

3. Copy artifacts to the production directory (`$ROCKET_ACTION` must be set):

   ```bash
   ./copy-app-to-prod.sh    # only the application JAR
   ./copy-all-to-prod.sh    # the application and all plugin JARs
   ```

## Contributing

Issues and pull requests are welcome.

- Keep the code compatible with **Java 17** and **Kotlin 1.8**.
- Follow the existing module structure and naming (`application-ui-swing`,
  `plugins/<name>`, and so on).
- Add new libraries to `gradle/libs.versions.toml` instead of hard-coding versions in
  module build scripts.
- Run `./gradlew test` before opening a pull request.
- Do not commit runtime data (`plugin-jars/`, `cache/`, `notes/`, `.rocket-action/`,
  `.jira-plugin/`).

## Acknowledgements

Icons and libraries used by the project:

| Project | Use |
|---------|-----|
| [useiconic](https://useiconic.com/open) | Icons |
| [Iconizer](https://iconizer.net/) | Icons |
| [Icons8](https://icons8.com/icons) | Icons |
| [image4j](https://github.com/imcdonagh/image4j) | Image formats |
| [Apache Batik](https://xmlgraphics.apache.org/batik/) | SVG rendering |
| [FlatLaf](https://github.com/JFormDesigner/FlatLaf) | Look and feel ([themes](https://www.formdev.com/flatlaf/themes/), [customizing](https://www.formdev.com/flatlaf/customizing/), [SwingX](https://github.com/JFormDesigner/FlatLaf/blob/main/flatlaf-swingx/README.md)) |
| [Swagger UI](https://github.com/swagger-api/swagger-ui) | API documentation UI |

## License

Released under the [MIT License](LICENSE).

Copyright (c) 2026 ezhov-da
