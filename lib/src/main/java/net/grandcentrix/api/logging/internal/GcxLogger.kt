package net.grandcentrix.api.logging.internal

import net.grandcentrix.api.logging.LogConfig
import net.grandcentrix.api.logging.LogLevel
import net.grandcentrix.api.logging.Logger

/**
 * Internal logger class. Filters log output by [level].
 */
internal class GcxLogger(
    private val level: LogLevel,
    private val logger: Logger
) {
    internal fun logV(tag: String, message: String, throwable: Throwable? = null) {
        if (LogLevel.Verbose.ordinal >= level.ordinal) {
            logger.v(tag, message, throwable)
        }
    }

    internal fun logD(tag: String, message: String, throwable: Throwable? = null) {
        if (LogLevel.Debug.ordinal >= level.ordinal) {
            logger.d(tag, message, throwable)
        }
    }

    internal fun logI(tag: String, message: String, throwable: Throwable? = null) {
        if (LogLevel.Info.ordinal >= level.ordinal) {
            logger.i(tag, message, throwable)
        }
    }

    internal fun logW(tag: String, message: String, throwable: Throwable? = null) {
        if (LogLevel.Warning.ordinal >= level.ordinal) {
            logger.w(tag, message, throwable)
        }
    }

    internal fun logE(tag: String, message: String, throwable: Throwable? = null) {
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
