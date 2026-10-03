package com.rezoxnemesis.muse

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rezoxnemesis.muse.data.MediaLibraryRepository
import com.rezoxnemesis.muse.data.MusePreferences
import com.rezoxnemesis.muse.data.Track
import com.rezoxnemesis.muse.data.UserPlaylist
import com.rezoxnemesis.muse.playback.MusePlaybackController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class LibraryState(
    val loading: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val error: String? = null,
)

data class SleepTimerState(
    val active: Boolean = false,
    val remainingMs: Long = 0L,
)

class MuseViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val libraryRepository = MediaLibraryRepository(application)
    private val preferences = MusePreferences(application)

    val playback = MusePlaybackController(application)

    private val _libraryState = MutableStateFlow(LibraryState())
    val libraryState: StateFlow<LibraryState> = _libraryState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val favoriteIds = preferences.favoriteIds.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptySet(),
    )

    val filteredTracks = combine(_libraryState, _searchQuery) { state, query ->
        if (query.isBlank()) {
            state.tracks
        } else {
            val needle = query.trim()
            state.tracks.filter { track ->
                track.title.contains(needle, ignoreCase = true) ||
                    track.artist.contains(needle, ignoreCase = true) ||
                    track.album.contains(needle, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val currentTrack = combine(_libraryState, playback.state) { library, playbackState ->
        playbackState.currentMediaId?.let { id -> library.tracks.firstOrNull { it.id == id } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    val likedTracks = combine(_libraryState, favoriteIds) { library, favourites ->
        library.tracks.filter { it.id in favourites }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val playlists = preferences.playlists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val recentTracks = combine(_libraryState, preferences.recentTrackIds) { library, ids ->
        val byId = library.tracks.associateBy { it.id }
        ids.mapNotNull(byId::get)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val localTrendingTracks = combine(
        _libraryState,
        favoriteIds,
        preferences.recentTrackIds,
    ) { library, favourites, recentIds ->
        val recentRank = recentIds.withIndex().associate { it.value to it.index }
        library.tracks.sortedWith(
            compareByDescending<Track> { it.id in favourites }
                .thenBy { recentRank[it.id] ?: Int.MAX_VALUE }
                .thenByDescending { it.dateAddedSeconds }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _selectedArtist = MutableStateFlow<String?>(null)
    val selectedArtist = _selectedArtist.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<String?>(null)
    val selectedAlbum = _selectedAlbum.asStateFlow()

    private val _sleepTimer = MutableStateFlow(SleepTimerState())
    val sleepTimer = _sleepTimer.asStateFlow()
    private var sleepTimerJob: Job? = null

    fun refreshLibrary() {
        viewModelScope.launch {
            _libraryState.value = _libraryState.value.copy(loading = true, error = null)
            runCatching { libraryRepository.loadTracks() }
                .onSuccess { tracks ->
                    _libraryState.value = LibraryState(tracks = tracks)
                }
                .onFailure { error ->
                    _libraryState.value = LibraryState(
                        tracks = _libraryState.value.tracks,
                        error = when (error) {
                            is SecurityException -> "Music access is required to scan your library."
                            else -> error.message ?: "Could not load the music library."
                        },
                    )
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playTrack(track: Track) {
        val tracks = _libraryState.value.tracks
        val index = tracks.indexOfFirst { it.id == track.id }
        if (index >= 0) {
            playback.playTracks(tracks, index)
            viewModelScope.launch { preferences.recordPlayed(track.id) }
        }
    }

    fun playTracks(tracks: List<Track>, startIndex: Int = 0) {
        playback.playTracks(tracks, startIndex)
    }

    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch { preferences.toggleFavorite(trackId) }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch { preferences.createPlaylist(name) }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch { preferences.deletePlaylist(playlistId) }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: Long) {
        viewModelScope.launch { preferences.addTrackToPlaylist(playlistId, trackId) }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: Long) {
        viewModelScope.launch { preferences.removeTrackFromPlaylist(playlistId, trackId) }
    }

    fun playlistTracks(playlist: UserPlaylist): List<Track> {
        val byId = _libraryState.value.tracks.associateBy { it.id }
        return playlist.trackIds.mapNotNull(byId::get)
    }

    fun selectArtist(name: String) {
        _selectedArtist.value = name
    }

    fun selectAlbum(name: String) {
        _selectedAlbum.value = name
    }

    fun artistTracks(name: String?): List<Track> =
        if (name == null) emptyList() else _libraryState.value.tracks.filter { it.artist == name }

    fun albumTracks(name: String?): List<Track> =
        if (name == null) emptyList() else _libraryState.value.tracks
            .filter { it.album == name }
            .sortedWith(compareBy<Track> { it.trackNumber ?: Int.MAX_VALUE }.thenBy { it.title })

    fun startSleepTimer(minutes: Int) {
        if (minutes <= 0) return
        sleepTimerJob?.cancel()
        val durationMs = minutes * 60_000L
        val deadline = SystemClock.elapsedRealtime() + durationMs
        _sleepTimer.value = SleepTimerState(active = true, remainingMs = durationMs)
        sleepTimerJob = viewModelScope.launch {
            while (isActive) {
                val remaining = (deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                _sleepTimer.value = SleepTimerState(active = remaining > 0L, remainingMs = remaining)
                if (remaining <= 0L) {
                    playback.pause()
                    break
                }
                delay(1_000)
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimer.value = SleepTimerState()
    }

    override fun onCleared() {
        playback.release()
        super.onCleared()
    }
}
