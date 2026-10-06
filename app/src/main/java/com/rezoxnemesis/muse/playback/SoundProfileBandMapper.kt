package com.rezoxnemesis.muse.playback

import kotlin.math.abs
import kotlin.math.ln

internal object SoundProfileBandMapper {
    fun mapLevels(
        savedCentersHz: IntArray,
        savedLevelsMb: IntArray,
        targetCentersHz: IntArray,
        minimumLevelMb: Int,
        maximumLevelMb: Int,
    ): IntArray {
        if (
            savedCentersHz.isEmpty() ||
            savedCentersHz.size != savedLevelsMb.size
        ) {
            return IntArray(targetCentersHz.size)
        }

        val minimum = minOf(minimumLevelMb, maximumLevelMb)
        val maximum = maxOf(minimumLevelMb, maximumLevelMb)

        return IntArray(targetCentersHz.size) { targetIndex ->
            val targetHz = targetCentersHz[targetIndex]
                .coerceAtLeast(1)

            val nearestSavedIndex = savedCentersHz.indices
                .minByOrNull { savedIndex ->
                    val savedHz = savedCentersHz[savedIndex]
                        .coerceAtLeast(1)
                    abs(
                        ln(
                            savedHz.toDouble() /
                                targetHz.toDouble()
                        )
                    )
                }
                ?: return@IntArray 0

            savedLevelsMb[nearestSavedIndex]
                .coerceIn(minimum, maximum)
        }
    }
}
