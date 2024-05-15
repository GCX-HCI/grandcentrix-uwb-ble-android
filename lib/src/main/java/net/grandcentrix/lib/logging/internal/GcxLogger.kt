package net.grandcentrix.lib.logging.internal

import net.grandcentrix.lib.logging.LogConfig
import net.grandcentrix.lib.logging.LogLevel
import net.grandcentrix.lib.logging.Logger
import net.grandcentrix.lib.logging.internal.GcxLogger.level

/**
 * Internal logger class. Filters log output by [level].
 */
internal object GcxLogger {
    private lateinit var level: LogLevel
    private lateinit var logger: Logger

    internal fun configure(logConfig: LogConfig) {
        level = logConfig.level
        logger = logConfig.logger
    }

    internal fun v(tag: String, message: String, throwable: Throwable? = null) = runInitialized {
        if (LogLevel.Verbose.ordinal >= level.ordinal) {
            logger.v(tag, message, throwable)
        }
    }

    internal fun d(tag: String, message: String, throwable: Throwable? = null) = runInitialized {
        if (LogLevel.Debug.ordinal >= level.ordinal) {
            logger.d(tag, message, throwable)
        }
    }

    internal fun i(tag: String, message: String, throwable: Throwable? = null) = runInitialized {
        if (LogLevel.Info.ordinal >= level.ordinal) {
            logger.i(tag, message, throwable)
        }
    }

    internal fun w(tag: String, message: String, throwable: Throwable? = null) = runInitialized {
        if (LogLevel.Warning.ordinal >= level.ordinal) {
            logger.w(tag, message, throwable)
        }
    }

    internal fun e(tag: String, message: String, throwable: Throwable? = null) = runInitialized {
        if (LogLevel.Error.ordinal >= level.ordinal) {
            logger.e(tag, message, throwable)
        }
    }

    private fun runInitialized(block: () -> Unit) {
        if (this::level.isInitialized && this::logger.isInitialized) {
            block()
        }
    }
}
