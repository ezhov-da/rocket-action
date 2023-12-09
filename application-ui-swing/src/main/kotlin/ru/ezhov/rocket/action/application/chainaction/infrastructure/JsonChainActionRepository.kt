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
    private val filePath = File("./.rocket-action/chain-actions.json")

    override fun save(chainAction: ChainAction) {
        val chains = all().toMutableList()

        val index = chains.indexOfFirst { it.id == chainAction.id }
        if (index == -1) {
            chains.add(chainAction)
        } else {
            chains[index] = chainAction
        }

        saveChains(chains)
    }

    private fun saveChains(chains: List<ChainAction>) {
        val actionsDto = ChainActionsDto(
            changedDate = LocalDateTime.now(),
            chainActions = chains.map { it.toChainActionDto() },
        )

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath, actionsDto)

        allCached = null
    }

    private var allCached: List<ChainAction>? = null

    override fun all(): List<ChainAction> {
        if (allCached == null) {
            allCached = when (filePath.exists()) {
                true ->
                    objectMapper.readValue(
                        filePath,
                        ChainActionsDto::class.java
                    ).chainActions.map { it.toChainAction() }

                false -> {
                    filePath.parentFile.mkdirs()
                    emptyList()
                }
            }
        }

        return allCached!!
    }

    override fun delete(id: String) {
        val actions = all().toMutableList()
        val index = actions.indexOfFirst { aa -> aa.id == id }
        if (index != -1) {
            actions.removeAt(index)
        }

        saveChains(actions)
    }

    private fun ChainAction.toChainActionDto(): ChainActionDto = ChainActionDto(
        id = id,
        name = name,
        description = description,
        actions = actions,
    )

    private fun ChainActionDto.toChainAction(): ChainAction = ChainAction(
        id = id,
        name = name,
        description = description,
        actions = actions,
    )
}
