package ru.ezhov.rocket.action.application.scheduler

import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service

@Service
class SchedulerSingleton : InitializingBean {
    init {
        Runtime.getRuntime().addShutdownHook(
            Thread {
                scheduler?.shutdown()
            }
        )
    }

    fun get(): Scheduler = scheduler!!

    companion object {
        private var scheduler: Scheduler? = null
        fun get(): Scheduler {
            initScheduler()

            return scheduler!!
        }

        private fun initScheduler() {
            if (scheduler == null) {
                scheduler = StdSchedulerFactory.getDefaultScheduler()
                scheduler!!.start()
            }
        }
    }

    override fun afterPropertiesSet() {
        initScheduler()
    }
}
