package ru.ezhov.rocket.action.application.chainaction.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionRepository
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.infrastructure.dto.ChainActionDto
import ru.ezhov.rocket.action.application.chainaction.infrastructure.dto.ChainActionsDto
import java.io.File
import java.time.LocalDateTime

@Primary
@Repository
class JsonChainActionRepository(
    private val objectMapper: ObjectMapper
) : ChainActionRepository {
    private val filePath = File("./chain-actions.json")

    override fun save(chainAction: ChainAction) {
        val chains = all().toMutableList()
        chains.add(chainAction)
        saveActions(chains)
    }

    private fun saveActions(chains: List<ChainAction>) {
        val actionsDto = ChainActionsDto(
            changedDate = LocalDateTime.now(),
            chainActions = chains.map { it.toChainActionDto() },
        )

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath, actionsDto)
    }

    override fun all(): List<ChainAction> =
        when (filePath.exists()) {
            true ->
                objectMapper.readValue(
                    filePath,
                    ChainActionsDto::class.java
                ).chainActions.map { it.toChainAction() }

            false -> emptyList()
        }

    override fun delete(id: String) {
        val actions = all().toMutableList()
        val index = actions.indexOfFirst { aa -> aa.id == id }
        if (index != -1) {
            actions.removeAt(index)
        }

        saveActions(actions)
    }

    private fun ChainAction.toChainActionDto(): ChainActionDto = ChainActionDto(
        id = id,
        name = name,
        description = description,
        actionIds = actionIds,
    )

    private fun ChainActionDto.toChainAction(): ChainAction = ChainAction(
        id = id,
        name = name,
        description = description,
        actionIds = actionIds,
    )
}
