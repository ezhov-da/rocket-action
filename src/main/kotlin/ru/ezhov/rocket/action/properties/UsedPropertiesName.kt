package ru.ezhov.rocket.action.properties

private const val PREFIX = "rocket.action"

enum class UsedPropertiesName(val propertyName: String, val description: String,) {
    VERSION("$PREFIX.version", "Версия приложения. Пример: 0.16.6"),
    INFO("$PREFIX.info", "Информация о приложении"),
    REPOSITORY("$PREFIX.repository", "Ссылка на репозиторий приложения"),

    FONT_NAME("$PREFIX.font.name", "Имя шрифта. Пример: Monospaced"),
    FONT_STYLE("$PREFIX.font.style", "Стиль шрифта. Пример: Значение константы java.awt.Font.PLAIN"),
    FONT_SIZE("$PREFIX.font.size", "Размер шрифта. Пример: 12"),

    CACHE_FOLDER("$PREFIX.cache.folder", "Папка для кэша. Пример: ./cache"),

    IS_DEVELOPER("$PREFIX.is.developer", "Режим разработчика. Пример: false"),

    DEFAULT_ACTIONS_FILE(
        "$PREFIX.default.actions.file",
        "Путь к файлу с действиями. Пример: ./actions.yml"
    ),

    UI_CONFIGURATION_DIALOG_WIDTH_IN_PERCENT(
        "$PREFIX.ui.configuration.dialog.width_in_percent",
        "Ширина окна конфигурации действий в процентах от ширины экрана. Пример: 0.6"
    ),
    UI_CONFIGURATION_DIALOG_HEIGHT_IN_PERCENT(
        "$PREFIX.ui.configuration.dialog.height_in_percent",
        "Высота окна конфигурации действий в процентах от высоты экрана. Пример: 0.6"
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
            
            Пример: com.formdev.flatlaf.FlatLightLaf
        """.trimIndent()
    ),
}