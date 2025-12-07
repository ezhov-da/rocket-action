package ru.ezhov.rocket.action.application.applicationConfiguration.infrastructure.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.VariablesManagerType

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonApplicationConfigurationsDto(
    var variablesKey: String?,
    var variablesManagers: List<JsonVariablesManager>?,
    var numberButtonsOnChainActionSelectionPanel: Int?,
    var globalHotKeys: JsonGlobalHotKeys?
)

data class JsonGlobalHotKeys(
    val activateSearchField: String?,
    val activateChainActionField: String?,
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = JsonVariablesManager.KeePassVariablesManager::class, name = "KEE_PASS"),
)
sealed class JsonVariablesManager(val type: VariablesManagerType) {
    class KeePassVariablesManager(
        val dbPath: String,
        val passwordVariableName: String,
        val variableRegExp: String,
    ) : JsonVariablesManager(type = VariablesManagerType.KEE_PASS)
}


