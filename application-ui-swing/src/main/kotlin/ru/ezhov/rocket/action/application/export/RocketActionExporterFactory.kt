package ru.ezhov.rocket.action.application.export

import java.io.File

object RocketActionExporterFactory {
    fun sqlLite(pathToDb: File): RocketActionExporter = SqliteExportService(pathToDb)
}
