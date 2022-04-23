package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import ru.ezhov.rocket.action.application.new_.infrastructure.db.DbCredentialsFactory
import java.io.File

class H2DbCredentialsFactorySampleData(
    override val url: String = "jdbc:h2:./../db/test/rocket-action/rocket-action",
    override val user: String = "rocket-action",
    override val password: String = "rocket-action",
) : DbCredentialsFactory {

    companion object {
        fun from(db: File) = H2DbCredentialsFactorySampleData(url = createUrl(db))

        private fun createUrl(db: File) = "jdbc:h2:${db.absolutePath}"
    }
}
