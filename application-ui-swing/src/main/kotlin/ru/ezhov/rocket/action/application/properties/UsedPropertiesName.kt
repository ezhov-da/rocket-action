package ru.ezhov.rocket.action.application.properties

private const val PREFIX = "rocket.action"

enum class UsedPropertiesName(val propertyName: String, val description: String) {
    VERSION("$PREFIX.version", "Application version. Example: 0.16.6"),
    INFO("$PREFIX.info", "App Information"),
    REPOSITORY("$PREFIX.repository", "Application repository link"),

    FONT_NAME("$PREFIX.font.name", "Font name. Example: Monospaced"),
    FONT_STYLE("$PREFIX.font.style", "Font style. Example: Value of a constant java.awt.Font.PLAIN"),
    FONT_SIZE("$PREFIX.font.size", "Font size. Example: 12"),

    CACHE_FOLDER("$PREFIX.cache.folder", "Cache folder. Example: ./cache"),

    IS_DEVELOPER("$PREFIX.is.developer", "Developer mode. Example: false"),

    DEFAULT_ACTIONS_FILE(
        "$PREFIX.default.actions.file",
        "The path to the action file. Example: ./actions.yml"
    ),

    UI_CONFIGURATION_DIALOG_WIDTH_IN_PERCENT(
        "$PREFIX.ui.configuration.dialog.width_in_percent",
        "The width of the action configuration window as a percentage of the screen width. Example: 0.6"
    ),
    UI_CONFIGURATION_DIALOG_HEIGHT_IN_PERCENT(
        "$PREFIX.ui.configuration.dialog.height_in_percent",
        "The height of the action configuration window as a percentage of the screen height. Example: 0.6"
    ),

    UI_CONFIGURATION_LOOK_AND_FEEL_CLASS(
        "$PREFIX.ui.configuration.look_and_feel.class",
        """
            LookAndFeel class flatlaf:
            FlatLaf Light - com.formdev.flatlaf.FlatLightLaf
            FlatLaf Dark - class com.formdev.flatlaf.FlatDarkLaf
            FlatLaf IntelliJ - class com.formdev.flatlaf.FlatIntelliJLaf
            FlatLaf Darcula  - com.formdev.flatlaf.FlatDarculaLaf
            Link: https://www.formdev.com/flatlaf/themes/

            Example: com.formdev.flatlaf.FlatLightLaf
        """.trimIndent()
    ),

    UI_CONFIGURATION_FRAME_ALWAYS_ON_TOP(
        "$PREFIX.ui.configuration.frame.always_on_top",
        """
            Display the configuration editor window always on top of windows.
            Example: false
        """.trimIndent()
    ),

    HANDLER_SERVER_PORT(
        "$PREFIX.handler.server.port",
        """
            Port for the handler server.
            Example: 4567
        """.trimIndent()
    ),

    VARIABLES_FILE_REPOSITORY_PATH(
        "$PREFIX.variables.file.repository.path",
        """
            Path to a file to store variables.
            Example:
            - relative path './folder/variables.json'
            - absolute path 'C:/folder/variables.json'
        """.trimIndent()
    ),

    APPLICATION_CONFIGURATION_FILE_REPOSITORY_PATH(
        "$PREFIX.application.configuration.file.repository.path",
        """
            Path for storing application settings.
            Example:
            - relative path './folder/configurations.json'
            - absolute path 'C:/folder/configurations.json'
        """.trimIndent()
    ),

    GROOVY_PLUGIN_FOLDER(
        "$PREFIX.groovy.plugin.folder",
        """
            Path to folder with plugins on Groovy
        """.trimIndent()
    ),

    KOTLIN_PLUGIN_FOLDER(
        "$PREFIX.kotlin.plugin.folder",
        """
            The path to the folder with plugins in Kotlin
        """.trimIndent()
    ),

    CHAIN_ACTION_ENABLE(
        "$PREFIX.chain-action.enabled",
        """
            Enable support for action chains
        """.trimIndent()
    ),

    UI_BASE_DIALOG_OPACITY(
        "$PREFIX.ui.base-dialog.opacity",
        """
            Setting the transparency of the basic dialog.
            Example: `0.5`. Min - 0.1. Max - 1
        """.trimIndent()
    ),
}
