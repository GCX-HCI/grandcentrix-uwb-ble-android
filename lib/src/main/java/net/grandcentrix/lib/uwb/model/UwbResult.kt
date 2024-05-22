package net.grandcentrix.lib.uwb.model

import androidx.core.uwb.RangingMeasurement
import androidx.core.uwb.RangingResult

sealed interface UwbResult {
    data object RangingStarted : UwbResult

    data object RangingStopped : UwbResult

    data class PositionResult(
        val distance: RangingMeasurement?,
        val azimuth: RangingMeasurement?,
        val elevation: RangingMeasurement?,
        val elapsedRealtimeNanos: Long
    ) : UwbResult

    data object Disconnected : UwbResult

    data object UnknownResult : UwbResult
}

internal fun RangingResult.RangingResultPosition.toPositionResult(): UwbResult =
    UwbResult.PositionResult(
        distance = position.distance,
        azimuth = position.azimuth,
        elevation = position.elevation,
        elapsedRealtimeNanos = position.elapsedRealtimeNanos
    )
