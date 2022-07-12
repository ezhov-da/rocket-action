package ru.ezhov.rocket.action.plugin.noteonfile.command

//Должен создаваться в рамках панели, чтоб не аффектить другие панели
class CommandObserver {
    private val listSaveTextCommandListener: MutableList<SaveTextCommandListener> = mutableListOf()

    fun sendCommand(command: SaveTextCommand) {
        listSaveTextCommandListener.forEach { it.save(command) }
    }

    fun register(listener: SaveTextCommandListener) {
        listSaveTextCommandListener.add(listener)
    }

}
