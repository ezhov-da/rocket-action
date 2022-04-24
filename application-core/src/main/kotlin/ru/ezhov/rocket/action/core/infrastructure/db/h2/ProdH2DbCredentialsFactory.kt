package ru.ezhov.rocket.action.core.infrastructure.db.h2

import ru.ezhov.rocket.action.core.infrastructure.db.DbCredentialsFactory

class ProdH2DbCredentialsFactory : DbCredentialsFactory {
    override val url: String
        get() = "jdbc:h2:./db/test/rocket-action/rocket-action"
    override val user: String
        get() = "rocket-action"
    override val password: String
        get() = "rocket-action"
}
