package ru.ezhov.rocket.action.application.hotkey

import com.tulskiy.keymaster.common.Provider

object HotKeyProviderSingleton {
    val PROVIDER: Provider = Provider.getCurrentProvider(false)

    init {
        Runtime.getRuntime().addShutdownHook(
            Thread {
                PROVIDER.reset()
                PROVIDER.stop()
            }
        )
    }
}
