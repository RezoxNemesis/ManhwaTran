package com.rezoxnemesis.muse.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.museDataStore by preferencesDataStore(name = "muse_preferences")
private val FavoriteTrackIds = stringSetPreferencesKey("favorite_track_ids")

class MusePreferences(
    private val context: Context,
) {
    val favoriteIds: Flow<Set<Long>> = context.museDataStore.data.map { prefs ->
        prefs[FavoriteTrackIds].orEmpty().mapNotNull(String::toLongOrNull).toSet()
    }

    suspend fun toggleFavorite(trackId: Long) {
        context.museDataStore.edit { prefs ->
            val current = prefs[FavoriteTrackIds].orEmpty().toMutableSet()
            val value = trackId.toString()
            if (!current.add(value)) current.remove(value)
            prefs[FavoriteTrackIds] = current
        }
    }
}
