package ru.ezhov.rocket.action.plugin.noteonfile.command

//Must be created within a panel so as not to affect other panels
class CommandObserver {
    private val listSaveTextCommandListener: MutableList<SaveTextCommandListener> = mutableListOf()

    fun sendCommand(command: SaveTextCommand) {
        listSaveTextCommandListener.forEach { it.save(command) }
    }

    fun register(listener: SaveTextCommandListener) {
        listSaveTextCommandListener.add(listener)
    }

}
