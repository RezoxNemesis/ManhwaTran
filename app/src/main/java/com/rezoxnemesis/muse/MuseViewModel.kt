package com.rezoxnemesis.muse

import android.app.Application
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rezoxnemesis.muse.data.LyricsDocument
import com.rezoxnemesis.muse.data.LyricsRepository
import com.rezoxnemesis.muse.data.ManagedMediaRepository
import com.rezoxnemesis.muse.data.MediaLibraryRepository
import com.rezoxnemesis.muse.data.MuseBackupRepository
import com.rezoxnemesis.muse.data.MusePreferences
import com.rezoxnemesis.muse.data.MuseSoundProfile
import com.rezoxnemesis.muse.data.Track
import com.rezoxnemesis.muse.data.UserPlaylist
import com.rezoxnemesis.muse.playback.MusePlaybackController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

enum class MuseMood {
    Chill,
    Workout,
    Love,
    Focus,
    Party,
    Sleep,
}

data class ManagedMediaUiState(
    val importing: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val error: String? = null,
)

data class BackupUiState(
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

class MuseViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val libraryRepository = MediaLibraryRepository(application)
    private val lyricsRepository = LyricsRepository(application)
    private val managedMediaRepository = ManagedMediaRepository(application)
    private val preferences = MusePreferences(application)
    private val backupRepository = MuseBackupRepository(
        application,
        preferences,
    )
    private var mediaStoreRefreshJob: Job? = null
    private val mediaStoreObserver = object : ContentObserver(
        Handler(Looper.getMainLooper()),
    ) {
        override fun onChange(selfChange: Boolean) {
            scheduleLibraryRefresh()
        }
    }

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

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val filteredTracks = combine(
        _libraryState,
        _searchQuery
            .debounce(140L)
            .distinctUntilChanged(),
    ) { state, query ->
        withContext(Dispatchers.Default) {
            if (query.isBlank()) {
                state.tracks
            } else {
                val needle = query.trim()
                state.tracks.filter { track ->
                    track.title.contains(needle, ignoreCase = true) ||
                        track.artist.contains(needle, ignoreCase = true) ||
                        track.album.contains(needle, ignoreCase = true) ||
                        track.albumArtist?.contains(needle, ignoreCase = true) == true ||
                        track.genre?.contains(needle, ignoreCase = true) == true
                }
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

    val soundProfiles = preferences.soundProfiles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val selectedSoundProfileId = preferences.selectedSoundProfileId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    val playCounts = preferences.playCounts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap(),
    )

    val topPlayedTracks = combine(
        _libraryState,
        playCounts,
    ) { library, counts ->
        library.tracks
            .map { track -> track to (counts[track.id] ?: 0) }
            .filter { (_, count) -> count > 0 }
            .sortedWith(
                compareByDescending<Pair<Track, Int>> { it.second }
                    .thenBy { it.first.title.lowercase() }
            )
            .take(10)
    }.stateIn(
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

    private val _managedMediaState = MutableStateFlow(ManagedMediaUiState())
    val managedMediaState = _managedMediaState.asStateFlow()

    private val _backupState = MutableStateFlow(BackupUiState())
    val backupState = _backupState.asStateFlow()

    init {
        application.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver,
        )

        viewModelScope.launch {
            combine(
                selectedSoundProfileId,
                soundProfiles,
                playback.audioEffects,
            ) { selectedId, profiles, effects ->
                Triple(
                    selectedId,
                    profiles.firstOrNull { it.id == selectedId },
                    effects.sessionReady,
                )
            }
                .distinctUntilChanged()
                .collect { (selectedId, profile, sessionReady) ->
                    if (
                        selectedId != null &&
                        profile != null &&
                        sessionReady
                    ) {
                        playback.applySoundProfile(profile)
                    }
                }
        }
    }

    private fun scheduleLibraryRefresh() {
        mediaStoreRefreshJob?.cancel()
        mediaStoreRefreshJob = viewModelScope.launch {
            delay(450L)
            refreshLibrary()
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _libraryState.value = _libraryState.value.copy(
                loading = true,
                error = null,
            )

            val deviceResult = runCatching {
                libraryRepository.loadTracks()
            }

            val managedUris = preferences.managedMediaUris.first()
            var unavailableManagedCount = 0
            val managedTracks = managedUris.mapNotNull { value ->
                runCatching {
                    managedMediaRepository.inspectAndPersist(Uri.parse(value))
                }.getOrElse {
                    unavailableManagedCount += 1
                    null
                }
            }

            _managedMediaState.value = ManagedMediaUiState(
                tracks = managedTracks,
                error = if (unavailableManagedCount > 0) {
                    "$unavailableManagedCount imported file(s) are currently unavailable."
                } else {
                    null
                },
            )

            val deviceTracks = deviceResult.getOrDefault(emptyList())
            val deviceError = deviceResult.exceptionOrNull()
            _libraryState.value = LibraryState(
                tracks = (deviceTracks + managedTracks)
                    .distinctBy { it.id }
                    .sortedByDescending { it.dateAddedSeconds },
                error = when (deviceError) {
                    null -> null
                    is SecurityException ->
                        "Device-library access is off. Imported files are still available."
                    else ->
                        deviceError.message ?: "Could not scan the device music library."
                },
            )
        }
    }

    fun importManagedMedia(uri: Uri) {
        viewModelScope.launch {
            _managedMediaState.value = _managedMediaState.value.copy(
                importing = true,
                error = null,
            )

            runCatching {
                managedMediaRepository.inspectAndPersist(uri)
            }
                .onSuccess { track ->
                    preferences.addManagedMediaUri(uri.toString())

                    val managedTracks = (_managedMediaState.value.tracks + track)
                        .distinctBy { it.id }
                        .sortedByDescending { it.dateAddedSeconds }
                    _managedMediaState.value = ManagedMediaUiState(
                        tracks = managedTracks,
                    )

                    _libraryState.value = _libraryState.value.copy(
                        tracks = (_libraryState.value.tracks + track)
                            .distinctBy { it.id }
                            .sortedByDescending { it.dateAddedSeconds },
                        error = null,
                    )
                }
                .onFailure { error ->
                    _managedMediaState.value = _managedMediaState.value.copy(
                        importing = false,
                        error = error.message ?: "Could not import the selected audio file.",
                    )
                }
        }
    }

    fun removeManagedMedia(track: Track) {
        if (!track.managedByMuse) return
        viewModelScope.launch {
            managedMediaRepository.releasePersistedAccess(track.uri)
            preferences.removeManagedMediaUri(track.uri.toString())
            preferences.removeTrackReferences(track.id)

            _managedMediaState.value = _managedMediaState.value.copy(
                tracks = _managedMediaState.value.tracks.filterNot { it.id == track.id },
                error = null,
            )
            _libraryState.value = _libraryState.value.copy(
                tracks = _libraryState.value.tracks.filterNot { it.id == track.id },
            )
            playback.removeQueueItemsByMediaId(track.id)
        }
    }

    fun clearManagedMediaError() {
        _managedMediaState.value = _managedMediaState.value.copy(error = null)
    }

    fun exportBackup(destination: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupUiState(busy = true)
            backupRepository.exportBackup(destination)
                .onSuccess {
                    _backupState.value = BackupUiState(
                        message = "Muse backup exported successfully.",
                    )
                }
                .onFailure { error ->
                    _backupState.value = BackupUiState(
                        error = error.message ?: "Could not export the Muse backup.",
                    )
                }
        }
    }

    fun restoreBackup(source: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupUiState(busy = true)
            backupRepository.restoreBackup(source)
                .onSuccess { summary ->
                    _backupState.value = BackupUiState(
                        message = buildString {
                            append("Restored ")
                            append(summary.playlists)
                            append(" playlists, ")
                            append(summary.soundProfiles)
                            append(" sound profiles and ")
                            append(summary.favorites)
                            append(" favourites.")
                        },
                    )
                }
                .onFailure { error ->
                    _backupState.value = BackupUiState(
                        error = error.message ?: "Could not restore the Muse backup.",
                    )
                }
        }
    }

    fun clearBackupMessage() {
        _backupState.value = BackupUiState()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playTrack(track: Track) {
        val tracks = _libraryState.value.tracks
        val index = tracks.indexOfFirst { it.id == track.id }
        if (index >= 0) {
            playback.playTracks(tracks, index)
        }
    }

    fun playTracks(tracks: List<Track>, startIndex: Int = 0) {
        playback.playTracks(tracks, startIndex)
    }

    fun playTracksInOrder(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        playback.setShuffleEnabled(false)
        playback.playTracks(tracks, 0)
    }

    fun playTracksShuffled(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        playback.setShuffleEnabled(true)
        playback.playTracks(tracks, 0)
    }

    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch { preferences.toggleFavorite(trackId) }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch { preferences.createPlaylist(name) }
    }

    fun saveCurrentSoundProfile(name: String) {
        val effects = playback.audioEffects.value
        if (!effects.connected || !effects.sessionReady) return

        viewModelScope.launch {
            preferences.saveSoundProfile(
                name = name,
                eqCentersHz = effects.bandCentersHz,
                eqLevelsMb = effects.bandLevelsMb,
                bassEnabled = effects.bassEnabled,
                bassStrength = effects.bassStrength,
                virtualizerEnabled = effects.virtualizerEnabled,
                virtualizerStrength = effects.virtualizerStrength,
                loudnessEnabled = effects.loudnessEnabled,
                loudnessGainMb = effects.loudnessGainMb,
            )
        }
    }

    fun applySoundProfile(profile: MuseSoundProfile) {
        playback.applySoundProfile(profile)
        viewModelScope.launch {
            preferences.selectSoundProfile(profile.id)
        }
    }

    fun clearSelectedSoundProfile() {
        viewModelScope.launch {
            preferences.selectSoundProfile(null)
        }
    }

    fun deleteSoundProfile(profileId: String) {
        viewModelScope.launch {
            preferences.deleteSoundProfile(profileId)
        }
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

    fun moodTracks(mood: MuseMood): List<Track> {
        val library = _libraryState.value.tracks
        if (library.isEmpty()) return emptyList()

        val favourites = favoriteIds.value
        val recentRank = recentTracks.value
            .mapIndexed { index, track -> track.id to index }
            .toMap()

        val keywords = when (mood) {
            MuseMood.Chill -> listOf("chill", "lofi", "lo-fi", "acoustic", "ambient", "soft", "calm")
            MuseMood.Workout -> listOf("workout", "gym", "run", "rock", "edm", "dance", "power")
            MuseMood.Love -> listOf("love", "heart", "romance", "romantic", "kiss")
            MuseMood.Focus -> listOf("focus", "study", "instrumental", "piano", "classical", "ambient")
            MuseMood.Party -> listOf("party", "club", "dance", "remix", "edm", "house")
            MuseMood.Sleep -> listOf("sleep", "night", "ambient", "calm", "piano", "dream")
        }

        return library
            .map { track ->
                val searchable = buildString {
                    append(track.title)
                    append(' ')
                    append(track.album)
                    append(' ')
                    append(track.artist)
                    track.albumArtist?.let {
                        append(' ')
                        append(it)
                    }
                    track.genre?.let {
                        append(' ')
                        append(it)
                    }
                }.lowercase()
                val keywordHits = keywords.count(searchable::contains)
                val durationScore = when (mood) {
                    MuseMood.Chill,
                    MuseMood.Focus,
                    MuseMood.Sleep -> if (track.durationMs >= 180_000L) 1 else 0
                    MuseMood.Workout,
                    MuseMood.Party -> if (track.durationMs in 120_000L..360_000L) 1 else 0
                    MuseMood.Love -> 0
                }
                val score =
                    keywordHits * 6 +
                    durationScore +
                    (if (track.id in favourites) 2 else 0) +
                    (if (track.id in recentRank) 1 else 0)

                track to score
            }
            .sortedWith(
                compareByDescending<Pair<Track, Int>> { it.second }
                    .thenBy { recentRank[it.first.id] ?: Int.MAX_VALUE }
                    .thenByDescending { it.first.dateAddedSeconds }
                    .thenBy { it.first.title.lowercase() }
            )
            .map { it.first }
            .take(30)
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

    fun loadLyrics(track: Track) {
        viewModelScope.launch {
            _lyricsState.value = LyricsUiState(
                trackId = track.id,
                loading = true,
            )
            runCatching {
                lyricsRepository.loadLyrics(
                    trackId = track.id,
                    mediaUri = track.uri,
                )
            }
                .onSuccess { document ->
                    _lyricsState.value = LyricsUiState(
                        trackId = track.id,
                        document = document,
                    )
                }
                .onFailure { error ->
                    _lyricsState.value = LyricsUiState(
                        trackId = track.id,
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
                    val track = _libraryState.value.tracks
                        .firstOrNull { it.id == trackId }
                    val document = lyricsRepository.loadLyrics(
                        trackId = trackId,
                        mediaUri = track?.uri,
                    )
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
        mediaStoreRefreshJob?.cancel()
        getApplication<Application>().contentResolver.unregisterContentObserver(
            mediaStoreObserver,
        )
        playback.release()
        super.onCleared()
    }
}
