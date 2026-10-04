package com.rezoxnemesis.muse.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MuseFlowEngineTest {
    private val engine = MuseFlowEngine()

    @Test
    fun buildQueue_keepsSeedFirstAndRanksStrongRelationshipsAheadOfUnrelatedTracks() {
        val seed = track(1, "Seed", "Artist A", "Album A", genre = "Rock", year = 2024)
        val sameArtist = track(2, "Same artist", "Artist A", "Album B", genre = "Pop", year = 2020)
        val sameGenre = track(3, "Same genre", "Artist C", "Album C", genre = "Rock", year = 2023)
        val unrelated = track(4, "Unrelated", "Artist D", "Album D", genre = "Ambient", year = 1990)

        val queue = engine.buildQueue(
            seedId = seed.id,
            library = listOf(unrelated, sameGenre, seed, sameArtist),
        )

        assertEquals(seed.id, queue.first())
        assertTrue(queue.indexOf(sameArtist.id) < queue.indexOf(sameGenre.id))
        assertTrue(queue.indexOf(sameGenre.id) < queue.indexOf(unrelated.id))
    }

    @Test
    fun buildQueue_demotesRecentlyPlayedAndHighSkipCandidates() {
        val seed = track(1, "Seed", "Artist A", "Album A", genre = "Rock", year = 2024)
        val fresh = track(2, "Fresh", "Artist B", "Album B", genre = "Rock", year = 2024)
        val fatigued = track(3, "Fatigued", "Artist B", "Album C", genre = "Rock", year = 2024)

        val queue = engine.buildQueue(
            seedId = seed.id,
            library = listOf(seed, fatigued, fresh),
            signals = MuseFlowSignals(
                recentTrackIds = listOf(fatigued.id),
                skipRatios = mapOf(fatigued.id to 0.9),
                favoriteIds = setOf(fatigued.id),
            ),
        )

        assertTrue(queue.indexOf(fresh.id) < queue.indexOf(fatigued.id))
    }

    @Test
    fun buildQueue_isDeterministicWithSparseMetadataAndNeverDuplicatesTracks() {
        val seed = track(10, "Seed", "", "", dateAdded = 10)
        val library = listOf(
            track(12, "C", "", "", dateAdded = 20),
            seed,
            track(11, "B", "", "", dateAdded = 30),
            track(11, "Duplicate id", "", "", dateAdded = 40),
            track(13, "A", "", "", dateAdded = 10),
        )

        val first = engine.buildQueue(seed.id, library, limit = 4)
        val second = engine.buildQueue(seed.id, library, limit = 4)

        assertEquals(first, second)
        assertEquals(first.distinct(), first)
        assertEquals(4, first.size)
        assertEquals(seed.id, first.first())
    }

    @Test
    fun buildQueue_respectsLimitAndReturnsEmptyForMissingSeed() {
        val library = (1L..8L).map { id ->
            track(id, "Track $id", "Artist $id", "Album $id")
        }

        val limited = engine.buildQueue(seedId = 1L, library = library, limit = 3)
        val missing = engine.buildQueue(seedId = 99L, library = library, limit = 3)

        assertEquals(3, limited.size)
        assertFalse(limited.contains(99L))
        assertTrue(missing.isEmpty())
    }

    private fun track(
        id: Long,
        title: String,
        artist: String,
        album: String,
        genre: String? = null,
        year: Int? = null,
        dateAdded: Long = id,
    ) = MuseFlowTrack(
        id = id,
        title = title,
        artist = artist,
        album = album,
        genre = genre,
        year = year,
        dateAddedSeconds = dateAdded,
    )
}
