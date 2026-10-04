package com.rezoxnemesis.muse.data

import kotlin.math.abs
import kotlin.math.ln

data class MuseFlowTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val dateAddedSeconds: Long = 0L,
)

data class MuseFlowSignals(
    val favoriteIds: Set<Long> = emptySet(),
    val recentTrackIds: List<Long> = emptyList(),
    val playCounts: Map<Long, Int> = emptyMap(),
    val completionRatios: Map<Long, Double> = emptyMap(),
    val skipRatios: Map<Long, Double> = emptyMap(),
    val sessionTrackIds: List<Long> = emptyList(),
    val positiveFeedbackIds: Set<Long> = emptySet(),
    val negativeFeedbackIds: Set<Long> = emptySet(),
)

/**
 * Deterministic, local-only queue builder for Muse Flow.
 *
 * The engine deliberately consumes only signals that Muse can honestly know.
 * Missing metadata does not block Flow: stable fallback ordering keeps the
 * feature useful on sparse or messy libraries without inventing acoustic data.
 */
class MuseFlowEngine {
    fun buildQueue(
        seedId: Long,
        library: List<MuseFlowTrack>,
        signals: MuseFlowSignals = MuseFlowSignals(),
        limit: Int = DefaultQueueLimit,
    ): List<Long> {
        if (limit <= 0) return emptyList()

        val uniqueLibrary = library.distinctBy(MuseFlowTrack::id)
        val seed = uniqueLibrary.firstOrNull { it.id == seedId } ?: return emptyList()
        if (limit == 1) return listOf(seed.id)

        val recentRank = signals.recentTrackIds
            .distinct()
            .withIndex()
            .associate { (index, id) -> id to index }
        val sessionRank = signals.sessionTrackIds
            .distinct()
            .withIndex()
            .associate { (index, id) -> id to index }

        val ranked = uniqueLibrary
            .asSequence()
            .filterNot { it.id == seed.id }
            .map { candidate ->
                RankedTrack(
                    track = candidate,
                    score = scoreCandidate(
                        seed = seed,
                        candidate = candidate,
                        signals = signals,
                        recentRank = recentRank,
                        sessionRank = sessionRank,
                    ),
                    recentRank = recentRank[candidate.id] ?: Int.MAX_VALUE,
                    yearDistance = yearDistance(seed.year, candidate.year),
                )
            }
            .sortedWith(
                compareByDescending<RankedTrack> { it.score }
                    .thenBy { it.recentRank }
                    .thenBy { it.yearDistance }
                    .thenByDescending { it.track.dateAddedSeconds }
                    .thenBy { it.track.title.lowercase() }
                    .thenBy { it.track.id }
            )
            .map(RankedTrack::track)
            .toList()

        val selected = diversify(
            seed = seed,
            candidates = ranked,
            targetCount = (limit - 1).coerceAtMost(ranked.size),
        )

        return buildList {
            add(seed.id)
            addAll(selected.map(MuseFlowTrack::id))
        }
    }

    private fun scoreCandidate(
        seed: MuseFlowTrack,
        candidate: MuseFlowTrack,
        signals: MuseFlowSignals,
        recentRank: Map<Long, Int>,
        sessionRank: Map<Long, Int>,
    ): Int {
        var score = 0

        if (sameMeaningful(seed.artist, candidate.artist)) score += 14
        if (sameMeaningful(seed.album, candidate.album)) score += 8
        if (sameMeaningful(seed.albumArtist, candidate.albumArtist)) score += 7
        if (sameMeaningful(seed.genre, candidate.genre)) score += 6

        val yearDelta = yearDistance(seed.year, candidate.year)
        score += when {
            yearDelta <= 1 -> 3
            yearDelta <= 4 -> 2
            yearDelta <= 8 -> 1
            else -> 0
        }

        if (candidate.id in signals.favoriteIds) score += 3
        if (candidate.id in signals.positiveFeedbackIds) score += 7
        if (candidate.id in signals.negativeFeedbackIds) score -= 14

        val playCount = signals.playCounts[candidate.id].orZero()
        score += when {
            playCount <= 0 -> 1
            playCount <= 2 -> 2
            else -> (ln(playCount.toDouble() + 1.0) * 1.7)
                .toInt()
                .coerceIn(2, 5)
        }

        val completion = signals.completionRatios[candidate.id]
            ?.coerceIn(0.0, 1.0)
        score += when {
            completion == null -> 0
            completion >= 0.90 -> 4
            completion >= 0.70 -> 3
            completion >= 0.50 -> 1
            else -> 0
        }

        val skip = signals.skipRatios[candidate.id]
            ?.coerceIn(0.0, 1.0)
        score -= when {
            skip == null -> 0
            skip >= 0.80 -> 9
            skip >= 0.60 -> 6
            skip >= 0.35 -> 3
            else -> 0
        }

        score -= when (recentRank[candidate.id]) {
            null -> 0
            in 0..3 -> 7
            in 4..9 -> 4
            in 10..24 -> 2
            else -> 1
        }

        if (candidate.id in sessionRank) score -= 12


        return score
    }

    private fun diversify(
        seed: MuseFlowTrack,
        candidates: List<MuseFlowTrack>,
        targetCount: Int,
    ): List<MuseFlowTrack> {
        if (targetCount <= 0) return emptyList()

        val selected = mutableListOf<MuseFlowTrack>()
        val deferred = mutableListOf<MuseFlowTrack>()
        var previousArtist = normalized(seed.artist)
        var consecutiveArtistCount = 1

        candidates.forEach { candidate ->
            val artist = normalized(candidate.artist)
            val repeatsArtist =
                artist.isNotBlank() &&
                    artist == previousArtist &&
                    consecutiveArtistCount >= MaxConsecutiveSameArtist

            if (repeatsArtist) {
                deferred += candidate
            } else if (selected.size < targetCount) {
                selected += candidate
                if (artist.isNotBlank() && artist == previousArtist) {
                    consecutiveArtistCount += 1
                } else {
                    previousArtist = artist
                    consecutiveArtistCount = 1
                }
            }
        }

        if (selected.size < targetCount) {
            selected += deferred.take(targetCount - selected.size)
        }

        return selected.take(targetCount)
    }

    private fun sameMeaningful(left: String?, right: String?): Boolean {
        val a = normalized(left)
        val b = normalized(right)
        return a.isNotBlank() && b.isNotBlank() && a == b
    }

    private fun normalized(value: String?): String =
        value
            ?.trim()
            ?.lowercase()
            ?.takeUnless {
                it == "unknown" ||
                    it == "unknown artist" ||
                    it == "<unknown>"
            }
            .orEmpty()

    private fun yearDistance(left: Int?, right: Int?): Int {
        if (left == null || right == null || left <= 0 || right <= 0) {
            return Int.MAX_VALUE
        }
        return abs(left - right)
    }

    private fun Int?.orZero(): Int = this ?: 0

    private data class RankedTrack(
        val track: MuseFlowTrack,
        val score: Int,
        val recentRank: Int,
        val yearDistance: Int,
    )

    private companion object {
        const val DefaultQueueLimit = 40
        const val MaxConsecutiveSameArtist = 2
    }
}
