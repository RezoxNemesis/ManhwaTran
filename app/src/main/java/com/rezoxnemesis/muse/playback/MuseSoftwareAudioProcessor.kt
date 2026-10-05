package com.rezoxnemesis.muse.playback

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Muse-owned PCM processing path.
 *
 * This intentionally does not depend on the OEM Equalizer/Spatializer stack, so
 * the curve EQ and "Muse Spatial 3D" compatibility mode remain available on
 * devices where Android's session Equalizer or Spatializer is missing.
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class MuseSoftwareAudioProcessor : BaseAudioProcessor() {

    @Volatile
    private var masterEnabled = true

    @Volatile
    private var bypass = false

    @Volatile
    private var spatialEnabled = false

    @Volatile
    private var spatialWidth = DefaultSpatialWidth

    @Volatile
    private var levelsMb = IntArray(BandCentersHz.size)

    @Volatile
    private var revision = 0L

    private var appliedRevision = Long.MIN_VALUE
    private var sampleRateHz = 48_000
    private var channelCount = 2

    private val leftFilters = Array(BandCentersHz.size) { Biquad() }
    private val rightFilters = Array(BandCentersHz.size) { Biquad() }

    fun setProcessingEnabled(
        enabled: Boolean,
        isBypassed: Boolean,
    ) {
        masterEnabled = enabled
        bypass = isBypassed
        revision++
    }

    fun setBandLevel(
        index: Int,
        levelMb: Int,
    ) {
        if (index !in BandCentersHz.indices) return
        val next = levelsMb.copyOf()
        next[index] = levelMb.coerceIn(MinLevelMb, MaxLevelMb)
        levelsMb = next
        revision++
    }

    fun setLevels(levels: IntArray) {
        val next = IntArray(BandCentersHz.size) { index ->
            levels.getOrNull(index)
                ?.coerceIn(MinLevelMb, MaxLevelMb)
                ?: 0
        }
        levelsMb = next
        revision++
    }

    fun currentLevels(): IntArray = levelsMb.copyOf()

    fun setSpatialEnabled(enabled: Boolean) {
        spatialEnabled = enabled
        revision++
    }

    fun isSpatialEnabled(): Boolean = spatialEnabled

    fun setSpatialWidth(value: Int) {
        spatialWidth = (
            MinSpatialWidth +
                (MaxSpatialWidth - MinSpatialWidth) *
                (value.coerceIn(0, 1000) / 1000f)
            )
        revision++
    }

    override fun onConfigure(
        inputAudioFormat: AudioProcessor.AudioFormat,
    ): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(
                inputAudioFormat,
            )
        }

        sampleRateHz = inputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount
        appliedRevision = Long.MIN_VALUE
        resetFilterMemory()
        return inputAudioFormat
    }

    override fun onFlush() {
        resetFilterMemory()
        appliedRevision = Long.MIN_VALUE
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val bytes = inputBuffer.remaining()
        if (bytes <= 0) return

        val output = replaceOutputBuffer(bytes)
        refreshCoefficientsIfNeeded()

        if (channelCount != 2) {
            output.put(inputBuffer)
            output.flip()
            return
        }

        val processEq = masterEnabled && !bypass
        val processSpatial = processEq && spatialEnabled
        val width = spatialWidth

        while (inputBuffer.remaining() >= StereoFrameBytes) {
            var left = inputBuffer.short.toFloat()
            var right = inputBuffer.short.toFloat()

            if (processEq) {
                for (index in BandCentersHz.indices) {
                    left = leftFilters[index].process(left)
                    right = rightFilters[index].process(right)
                }
            }

            if (processSpatial) {
                // Mid/side widening is deterministic, low-latency and works on
                // ordinary stereo PCM even when the device Spatializer reports
                // "not supported". Keep some headroom to avoid hard clipping.
                val mid = (left + right) * 0.5f
                val side = (left - right) * 0.5f * width
                left = (mid + side) * SpatialHeadroom
                right = (mid - side) * SpatialHeadroom
            }

            output.putShort(left.toPcm16())
            output.putShort(right.toPcm16())
        }

        // Defensive passthrough for an incomplete trailing frame.
        while (inputBuffer.hasRemaining()) {
            output.put(inputBuffer.get())
        }

        output.flip()
    }

    private fun refreshCoefficientsIfNeeded() {
        val localRevision = revision
        if (appliedRevision == localRevision) return

        val gains = levelsMb
        BandCentersHz.forEachIndexed { index, centerHz ->
            val db = gains.getOrElse(index) { 0 } / 100f
            val frequency = centerHz
                .coerceAtMost((sampleRateHz * 0.45f).toInt())
                .coerceAtLeast(20)
            leftFilters[index].configurePeaking(
                sampleRate = sampleRateHz,
                frequencyHz = frequency.toFloat(),
                gainDb = db,
                q = BandQ[index],
            )
            rightFilters[index].configurePeaking(
                sampleRate = sampleRateHz,
                frequencyHz = frequency.toFloat(),
                gainDb = db,
                q = BandQ[index],
            )
        }
        appliedRevision = localRevision
    }

    private fun resetFilterMemory() {
        leftFilters.forEach(Biquad::reset)
        rightFilters.forEach(Biquad::reset)
    }

    private fun Float.toPcm16(): Short =
        coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat())
            .toInt()
            .toShort()

    private class Biquad {
        private var b0 = 1f
        private var b1 = 0f
        private var b2 = 0f
        private var a1 = 0f
        private var a2 = 0f
        private var z1 = 0f
        private var z2 = 0f

        fun configurePeaking(
            sampleRate: Int,
            frequencyHz: Float,
            gainDb: Float,
            q: Float,
        ) {
            if (sampleRate <= 0 || frequencyHz <= 0f) {
                b0 = 1f
                b1 = 0f
                b2 = 0f
                a1 = 0f
                a2 = 0f
                return
            }

            val a = 10.0.pow(gainDb / 40.0)
            val omega = 2.0 * PI * frequencyHz / sampleRate
            val alpha = sin(omega) / (2.0 * q)
            val cosOmega = cos(omega)

            val rb0 = 1.0 + alpha * a
            val rb1 = -2.0 * cosOmega
            val rb2 = 1.0 - alpha * a
            val ra0 = 1.0 + alpha / a
            val ra1 = -2.0 * cosOmega
            val ra2 = 1.0 - alpha / a

            b0 = (rb0 / ra0).toFloat()
            b1 = (rb1 / ra0).toFloat()
            b2 = (rb2 / ra0).toFloat()
            a1 = (ra1 / ra0).toFloat()
            a2 = (ra2 / ra0).toFloat()
        }

        fun process(input: Float): Float {
            val output = b0 * input + z1
            z1 = b1 * input - a1 * output + z2
            z2 = b2 * input - a2 * output
            return output
        }

        fun reset() {
            z1 = 0f
            z2 = 0f
        }
    }

    companion object {
        val BandCentersHz = intArrayOf(
            60,
            150,
            400,
            1_000,
            2_400,
            6_000,
            14_000,
        )
        val BandQ = floatArrayOf(
            0.82f,
            0.90f,
            1.00f,
            1.05f,
            1.10f,
            1.12f,
            0.92f,
        )

        const val MinLevelMb = -1_500
        const val MaxLevelMb = 1_500

        private const val StereoFrameBytes = 4
        private const val MinSpatialWidth = 1.0f
        private const val MaxSpatialWidth = 1.62f
        private const val DefaultSpatialWidth = 1.42f
        private const val SpatialHeadroom = 0.82f
    }
}
