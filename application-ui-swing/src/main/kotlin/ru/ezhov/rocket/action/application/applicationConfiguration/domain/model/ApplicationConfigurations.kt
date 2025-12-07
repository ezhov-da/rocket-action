package ru.ezhov.rocket.action.application.applicationConfiguration.domain.model

data class ApplicationConfigurations(
    var variablesKey: String,
    var variablesManagers: List<VariablesManager>,
    var numberButtonsOnChainActionSelectionPanel: Int,
    var globalHotKeys: GlobalHotKeys?,
)

data class GlobalHotKeys(
    val activateSearchField: String?,
    val activateChainActionField: String?,
)

sealed class VariablesManager(val type: VariablesManagerType) {
    class KeePassVariablesManager(
        val dbPath: String,
        val passwordVariableName: String,
        val variableRegExp: String,
    ) : VariablesManager(type = VariablesManagerType.KEE_PASS) {
        override fun toString(): String {
            return "${KeePassVariablesManager::class.qualifiedName}(" +
                "${KeePassVariablesManager::dbPath.name}='$dbPath', " +
                "${KeePassVariablesManager::passwordVariableName.name}='$passwordVariableName', " +
                "${KeePassVariablesManager::variableRegExp.name}='$variableRegExp'" +
                ")"
        }
    }
}

enum class VariablesManagerType {
    KEE_PASS
}
