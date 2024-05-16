package net.grandcentrix.lib.logging.internal

import net.grandcentrix.lib.logging.LogConfig
import net.grandcentrix.lib.logging.LogLevel
import net.grandcentrix.lib.logging.Logger

/**
 * Internal logger class. Filters log output by [level].
 */
internal class GcxLogger(
    private val level: LogLevel,
    private val logger: Logger
) {
    internal fun v(tag: String, message: String, throwable: Throwable? = null) {
        if (LogLevel.Verbose.ordinal >= level.ordinal) {
            logger.v(tag, message, throwable)
        }
    }

    internal fun d(tag: String, message: String, throwable: Throwable? = null) {
        if (LogLevel.Debug.ordinal >= level.ordinal) {
            logger.d(tag, message, throwable)
        }
    }

    internal fun i(tag: String, message: String, throwable: Throwable? = null) {
        if (LogLevel.Info.ordinal >= level.ordinal) {
            logger.i(tag, message, throwable)
        }
    }

    internal fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (LogLevel.Warning.ordinal >= level.ordinal) {
            logger.w(tag, message, throwable)
        }
    }

    internal fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (LogLevel.Error.ordinal >= level.ordinal) {
            logger.e(tag, message, throwable)
        }
    }

    companion object {
        fun initialize(logConfig: LogConfig): GcxLogger {
            return GcxLogger(logConfig.level, logConfig.logger)
        }
    }
}
