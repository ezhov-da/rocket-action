package ru.ezhov.rocket.action.application.chainaction.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType

@JsonIgnoreProperties(ignoreUnknown = true)
class AtomicActionDto(
    var id: String,
    var name: String,
    var description: String,
    var contractType: ContractType = ContractType.IN_OUT,
    var engine: AtomicActionEngine,
    var source: AtomicActionSource,
    var data: String,
    var alias: String? = null,
)
