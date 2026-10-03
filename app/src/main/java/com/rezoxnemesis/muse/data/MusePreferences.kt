package com.rezoxnemesis.muse.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.museDataStore by preferencesDataStore(name = "muse_preferences")
private val FavoriteTrackIds = stringSetPreferencesKey("favorite_track_ids")
private val RecentTrackIds = stringPreferencesKey("recent_track_ids")
private val PlaylistsJson = stringPreferencesKey("playlists_json")

data class UserPlaylist(
    val id: String,
    val name: String,
    val trackIds: List<Long>,
)

class MusePreferences(
    private val context: Context,
) {
    val favoriteIds: Flow<Set<Long>> = context.museDataStore.data.map { prefs ->
        prefs[FavoriteTrackIds].orEmpty().mapNotNull(String::toLongOrNull).toSet()
    }

    val recentTrackIds: Flow<List<Long>> = context.museDataStore.data.map { prefs ->
        prefs[RecentTrackIds]
            .orEmpty()
            .split(',')
            .mapNotNull(String::toLongOrNull)
    }

    val playlists: Flow<List<UserPlaylist>> = context.museDataStore.data.map { prefs ->
        decodePlaylists(prefs[PlaylistsJson].orEmpty())
    }

    suspend fun toggleFavorite(trackId: Long) {
        context.museDataStore.edit { prefs ->
            val current = prefs[FavoriteTrackIds].orEmpty().toMutableSet()
            val value = trackId.toString()
            if (!current.add(value)) current.remove(value)
            prefs[FavoriteTrackIds] = current
        }
    }

    suspend fun recordPlayed(trackId: Long) {
        context.museDataStore.edit { prefs ->
            val current = prefs[RecentTrackIds]
                .orEmpty()
                .split(',')
                .mapNotNull(String::toLongOrNull)
                .toMutableList()
            current.remove(trackId)
            current.add(0, trackId)
            prefs[RecentTrackIds] = current.take(100).joinToString(",")
        }
    }

    suspend fun createPlaylist(name: String) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        mutatePlaylists { current ->
            current + UserPlaylist(
                id = UUID.randomUUID().toString(),
                name = cleanName,
                trackIds = emptyList(),
            )
        }
    }

    suspend fun deletePlaylist(playlistId: String) {
        mutatePlaylists { current -> current.filterNot { it.id == playlistId } }
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: Long) {
        mutatePlaylists { current ->
            current.map { playlist ->
                if (playlist.id != playlistId || trackId in playlist.trackIds) playlist
                else playlist.copy(trackIds = playlist.trackIds + trackId)
            }
        }
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: Long) {
        mutatePlaylists { current ->
            current.map { playlist ->
                if (playlist.id != playlistId) playlist
                else playlist.copy(trackIds = playlist.trackIds.filterNot { it == trackId })
            }
        }
    }

    private suspend fun mutatePlaylists(
        transform: (List<UserPlaylist>) -> List<UserPlaylist>,
    ) {
        context.museDataStore.edit { prefs ->
            val current = decodePlaylists(prefs[PlaylistsJson].orEmpty())
            prefs[PlaylistsJson] = encodePlaylists(transform(current))
        }
    }

    private fun encodePlaylists(playlists: List<UserPlaylist>): String {
        val array = JSONArray()
        playlists.forEach { playlist ->
            val tracks = JSONArray()
            playlist.trackIds.forEach { tracks.put(it) }
            array.put(
                JSONObject()
                    .put("id", playlist.id)
                    .put("name", playlist.name)
                    .put("tracks", tracks)
            )
        }
        return array.toString()
    }

    private fun decodePlaylists(raw: String): List<UserPlaylist> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val tracks = item.optJSONArray("tracks") ?: JSONArray()
                    val trackIds = buildList {
                        for (trackIndex in 0 until tracks.length()) {
                            val value = tracks.optLong(trackIndex, -1L)
                            if (value >= 0L) add(value)
                        }
                    }
                    val id = item.optString("id")
                    val name = item.optString("name")
                    if (id.isNotBlank() && name.isNotBlank()) {
                        add(UserPlaylist(id = id, name = name, trackIds = trackIds))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }
}
