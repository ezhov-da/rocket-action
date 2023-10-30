package ru.ezhov.rocket.action.application.chainaction.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import ru.ezhov.rocket.action.application.chainaction.domain.AtomicActionRepository
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.infrastructure.dto.AtomicActionDto
import ru.ezhov.rocket.action.application.chainaction.infrastructure.dto.AtomicActionsDto
import java.io.File
import java.time.LocalDateTime

@Primary
@Repository
class JsonAtomicActionRepository(
    private val objectMapper: ObjectMapper
) : AtomicActionRepository {
    private val filePath = File("./atomic-actions.json")

    override fun save(atomicAction: AtomicAction) {
        val actions = all().toMutableList()

        val index = actions.indexOfFirst { it.id == atomicAction.id }
        if (index == -1) {
            actions.add(atomicAction)
        } else {
            actions[index] = atomicAction
        }

        saveActions(actions)
    }

    private fun saveActions(actions: List<AtomicAction>) {
        val actionsDto = AtomicActionsDto(
            changedDate = LocalDateTime.now(),
            atomicActions = actions.map { it.toAtomicActionDto() },
        )

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath, actionsDto)
    }

    override fun all(): List<AtomicAction> =
        when (filePath.exists()) {
            true ->
                objectMapper.readValue(
                    filePath,
                    AtomicActionsDto::class.java
                ).atomicActions.map { it.toAtomicActionDto() }

            false -> emptyList()
        }

    override fun byId(id: String): AtomicAction? = all().firstOrNull { it.id == id }

    override fun delete(id: String) {
        val actions = all().toMutableList()
        val index = actions.indexOfFirst { aa -> aa.id == id }
        if (index != -1) {
            actions.removeAt(index)
        }

        saveActions(actions)
    }

    private fun AtomicAction.toAtomicActionDto(): AtomicActionDto = AtomicActionDto(
        id = id,
        name = name,
        description = description,
        contractType = contractType,
        engine = engine,
        source = source,
        data = data,
    )

    private fun AtomicActionDto.toAtomicActionDto(): AtomicAction = AtomicAction(
        id = id,
        name = name,
        description = description,
        contractType = contractType,
        engine = engine,
        source = source,
        data = data,
    )
}
