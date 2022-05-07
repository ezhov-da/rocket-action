package ru.ezhov.rocket.action.core

import org.junit.Ignore
import org.junit.Test
import ru.ezhov.rocket.action.core.infrastructure.db.LiquibaseDbPreparedService
import java.io.File

class DbService {
    @Test
    @Ignore
    fun `create db`() {
        LiquibaseDbPreparedService.prepareH2Db(file = File("./test-db"))
    }
}
