package net.grandcentrix.api.logging

/**
 * Interface defining a logging system with various log levels.
 */
interface Logger {

    /**
     * Verbose log level.
     * @param tag The tag identifying the source of the log message.
     * @param message The message to be logged.
     * @param throwable An optional throwable associated with the log message.
     */
    fun v(tag: String, message: String, throwable: Throwable?)

    /**
     * Debug log level. Used for logging messages that are helpful in debugging the application.
     * @param tag The tag identifying the source of the log message.
     * @param message The message to be logged.
     * @param throwable An optional throwable associated with the log message.
     */
    fun d(tag: String, message: String, throwable: Throwable?)

    /**
     * Information log level. Used for logging informational messages about the application.
     * @param tag The tag identifying the source of the log message.
     * @param message The message to be logged.
     * @param throwable An optional throwable associated with the log message.
     */
    fun i(tag: String, message: String, throwable: Throwable?)

    /**
     * Warning log level. Used for logging warning messages that may indicate potential issues.
     * @param tag The tag identifying the source of the log message.
     * @param message The message to be logged.
     * @param throwable An optional throwable associated with the log message.
     */
    fun w(tag: String, message: String, throwable: Throwable?)

    /**
     * Error log level. Used for logging error messages indicating failure or unexpected behavior.
     * @param tag The tag identifying the source of the log message.
     * @param message The message to be logged.
     * @param throwable An optional throwable associated with the log message.
     */
    fun e(tag: String, message: String, throwable: Throwable?)
}
