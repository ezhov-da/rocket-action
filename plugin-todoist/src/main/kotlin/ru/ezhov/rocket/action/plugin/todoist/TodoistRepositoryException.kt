package ru.ezhov.rocket.action.plugin.todoist

class TodoistRepositoryException : Exception {
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(message: String?) : super(message) {}
}