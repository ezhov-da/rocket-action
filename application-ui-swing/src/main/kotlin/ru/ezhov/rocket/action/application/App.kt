package ru.ezhov.rocket.action.application

fun main(args: Array<String>) {
    CommandLineArgsSingleton.args = args

    val appRunService: AppRunService = ApplicationContextFactory.context().getBean(AppRunService::class.java)
    appRunService.run(args)
}
