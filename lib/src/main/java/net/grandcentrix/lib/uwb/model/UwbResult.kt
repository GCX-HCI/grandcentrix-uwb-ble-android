package net.grandcentrix.lib.uwb.model

import androidx.core.uwb.RangingMeasurement
import androidx.core.uwb.RangingResult

/**
 * Sealed interface representing the various possible results of UWB session.
 */
sealed interface UwbResult {

    /**
     * Represents the state when UWB ranging has started successfully.
     */
    data object RangingStarted : UwbResult

    /**
     * Represents the state when UWB ranging has been stopped.
     */
    data object RangingStopped : UwbResult

    /**
     * Represents a position result obtained from UWB ranging.
     *
     * @property distance The measured distance from the controller, or null if not available.
     * @property azimuth The measured azimuth from the controller, or null if not available.
     * @property elevation The measured elevation from the controller, or null if not available.
     * @property elapsedRealtimeNanos The elapsed real-time in nanoseconds since the ranging measurement was taken.
     */
    data class PositionResult(
        val distance: RangingMeasurement?,
        val azimuth: RangingMeasurement?,
        val elevation: RangingMeasurement?,
        val elapsedRealtimeNanos: Long
    ) : UwbResult

    /**
     * Represents the state when UWB is disconnected.
     */
    data object Disconnected : UwbResult

    /**
     * Represents an unknown result from UWB operations.
     */
    data object UnknownResult : UwbResult
}

internal fun RangingResult.RangingResultPosition.toPositionResult(): UwbResult =
    UwbResult.PositionResult(
        distance = position.distance,
        azimuth = position.azimuth,
        elevation = position.elevation,
        elapsedRealtimeNanos = position.elapsedRealtimeNanos
    )
