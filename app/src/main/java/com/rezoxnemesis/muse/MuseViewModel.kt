package com.rezoxnemesis.muse

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rezoxnemesis.muse.data.LyricsDocument
import com.rezoxnemesis.muse.data.LyricsRepository
import com.rezoxnemesis.muse.data.MediaLibraryRepository
import com.rezoxnemesis.muse.data.MusePreferences
import com.rezoxnemesis.muse.data.Track
import com.rezoxnemesis.muse.data.UserPlaylist
import com.rezoxnemesis.muse.playback.MusePlaybackController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryState(
    val loading: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val error: String? = null,
)

data class LyricsUiState(
    val trackId: Long? = null,
    val loading: Boolean = false,
    val document: LyricsDocument? = null,
    val error: String? = null,
)

class MuseViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val libraryRepository = MediaLibraryRepository(application)
    private val lyricsRepository = LyricsRepository(application)
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

    private val _selectedPlaylistId = MutableStateFlow<String?>(null)
    val selectedPlaylistId = _selectedPlaylistId.asStateFlow()

    private val _lyricsState = MutableStateFlow(LyricsUiState())
    val lyricsState = _lyricsState.asStateFlow()

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

    fun saveCurrentQueueAsPlaylist(name: String) {
        val trackIds = playback.state.value.queue.mapNotNull { it.mediaId }
        if (trackIds.isEmpty()) return
        viewModelScope.launch {
            preferences.createPlaylist(name, trackIds)
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch { preferences.deletePlaylist(playlistId) }
    }

    fun renamePlaylist(playlistId: String, name: String) {
        viewModelScope.launch { preferences.renamePlaylist(playlistId, name) }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: Long) {
        viewModelScope.launch { preferences.addTrackToPlaylist(playlistId, trackId) }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: Long) {
        viewModelScope.launch { preferences.removeTrackFromPlaylist(playlistId, trackId) }
    }

    fun moveTrackInPlaylist(playlistId: String, fromIndex: Int, toIndex: Int) {
        viewModelScope.launch { preferences.moveTrackInPlaylist(playlistId, fromIndex, toIndex) }
    }

    fun playLocalSongRadio(seed: Track) {
        val library = _libraryState.value.tracks
        val favourites = favoriteIds.value
        val recents = recentTracks.value.map { it.id }.toSet()

        val related = library
            .asSequence()
            .filter { it.id != seed.id }
            .map { candidate ->
                val score =
                    (if (candidate.artist == seed.artist) 6 else 0) +
                    (if (candidate.album == seed.album) 4 else 0) +
                    (if (candidate.id in favourites) 2 else 0) +
                    (if (candidate.id in recents) 1 else 0)
                candidate to score
            }
            .filter { (_, score) -> score > 0 }
            .sortedWith(
                compareByDescending<Pair<Track, Int>> { it.second }
                    .thenByDescending { it.first.dateAddedSeconds }
                    .thenBy { it.first.title.lowercase() }
            )
            .map { it.first }
            .take(40)
            .toList()

        val radio = listOf(seed) + related
        playback.playTracks(radio, 0)
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

    fun selectPlaylist(id: String) {
        _selectedPlaylistId.value = id
    }

    fun artistTracks(name: String?): List<Track> =
        if (name == null) emptyList() else _libraryState.value.tracks.filter { it.artist == name }

    fun albumTracks(name: String?): List<Track> =
        if (name == null) emptyList() else _libraryState.value.tracks
            .filter { it.album == name }
            .sortedWith(compareBy<Track> { it.trackNumber ?: Int.MAX_VALUE }.thenBy { it.title })

    fun loadLyrics(trackId: Long) {
        viewModelScope.launch {
            _lyricsState.value = LyricsUiState(trackId = trackId, loading = true)
            runCatching { lyricsRepository.loadLyrics(trackId) }
                .onSuccess { document ->
                    _lyricsState.value = LyricsUiState(
                        trackId = trackId,
                        document = document,
                    )
                }
                .onFailure { error ->
                    _lyricsState.value = LyricsUiState(
                        trackId = trackId,
                        error = error.message ?: "Could not load lyrics.",
                    )
                }
        }
    }

    fun importLyrics(trackId: Long, source: Uri) {
        viewModelScope.launch {
            _lyricsState.value = _lyricsState.value.copy(
                trackId = trackId,
                loading = true,
                error = null,
            )
            lyricsRepository.importLyrics(trackId, source)
                .onSuccess {
                    val document = lyricsRepository.loadLyrics(trackId)
                    _lyricsState.value = LyricsUiState(
                        trackId = trackId,
                        document = document,
                    )
                }
                .onFailure { error ->
                    _lyricsState.value = LyricsUiState(
                        trackId = trackId,
                        error = error.message ?: "Could not import lyrics.",
                    )
                }
        }
    }

    fun removeLyrics(trackId: Long) {
        viewModelScope.launch {
            val removed = lyricsRepository.removeLyrics(trackId)
            _lyricsState.value = if (removed) {
                LyricsUiState(trackId = trackId)
            } else {
                LyricsUiState(
                    trackId = trackId,
                    error = "Could not remove the imported lyrics.",
                )
            }
        }
    }

    override fun onCleared() {
        playback.release()
        super.onCleared()
    }
}
