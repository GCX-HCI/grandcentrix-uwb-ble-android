package net.grandcentrix.api.logging

/**
 * Interface defining configuration for logging.
 */
interface LogConfig {
    /**
     * Log messages are filtered by [level]. If set to [LogLevel.Verbose] everything will be logged.
     * If set to [LogLevel.Warning] only [LogLevel.Warning] and [LogLevel.Error] will be logged.
     */
    val level: LogLevel

    /**
     * Property representing the logger instance to be used for logging.
     */
    val logger: Logger
}

/**
 * Default implementation of [LogConfig] representing the default logging configuration.
 *
 * @property level The log level which will be used to filter the log output. Defaults to [LogLevel.Debug].
 * @property logger The logger instance to be used for logging. Defaults to [AndroidLogger].
 */
data class DefaultLogConfig(
    override val level: LogLevel = LogLevel.Debug,
    override val logger: Logger = AndroidLogger
) : LogConfig
