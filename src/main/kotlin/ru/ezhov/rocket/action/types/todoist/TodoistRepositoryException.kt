package ru.ezhov.rocket.action.types.todoist

class TodoistRepositoryException : Exception {
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(message: String?) : super(message) {}
}