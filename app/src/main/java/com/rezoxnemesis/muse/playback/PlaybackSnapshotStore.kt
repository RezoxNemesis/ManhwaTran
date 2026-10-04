package com.rezoxnemesis.muse.playback

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import org.json.JSONArray
import org.json.JSONObject

private const val PreferencesName = "muse_playback_session"
private const val SnapshotKey = "snapshot"
private const val SnapshotVersion = 4
private const val SourceUriExtraKey = "muse.source_uri"
private const val BitrateExtraKey = "muse.bitrate_bps"
private const val SampleRateExtraKey = "muse.sample_rate_hz"

class PlaybackSnapshotStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    fun save(player: Player) {
        if (player.mediaItemCount == 0) {
            preferences.edit().remove(SnapshotKey).apply()
            return
        }

        val queue = JSONArray()
        repeat(player.mediaItemCount) { index ->
            val item = player.getMediaItemAt(index)
            val id = item.mediaId.toLongOrNull() ?: return@repeat
            queue.put(
                JSONObject()
                    .put("id", id)
                    .put("title", item.mediaMetadata.title?.toString().orEmpty())
                    .put("artist", item.mediaMetadata.artist?.toString().orEmpty())
                    .put("album", item.mediaMetadata.albumTitle?.toString().orEmpty())
                    .put("albumArtist", item.mediaMetadata.albumArtist?.toString().orEmpty())
                    .put("genre", item.mediaMetadata.genre?.toString().orEmpty())
                    .put(
                        "artworkUri",
                        item.mediaMetadata.artworkUri
                            ?.toString()
                            .orEmpty(),
                    )
                    .put(
                        "bitrateBps",
                        item.mediaMetadata.extras
                            ?.getInt(BitrateExtraKey, 0)
                            ?: 0,
                    )
                    .put(
                        "sampleRateHz",
                        item.mediaMetadata.extras
                            ?.getInt(SampleRateExtraKey, 0)
                            ?: 0,
                    )
                    .put(
                        "uri",
                        item.mediaMetadata.extras
                            ?.getString(SourceUriExtraKey)
                            .orEmpty(),
                    )
            )
        }

        if (queue.length() == 0) {
            preferences.edit().remove(SnapshotKey).apply()
            return
        }

        val payload = JSONObject()
            .put("version", SnapshotVersion)
            .put("queue", queue)
            .put("index", player.currentMediaItemIndex.coerceAtLeast(0))
            .put("positionMs", player.currentPosition.coerceAtLeast(0L))
            .put("repeatMode", player.repeatMode)
            .put("shuffle", player.shuffleModeEnabled)
            .put("savedAtMs", System.currentTimeMillis())

        preferences.edit().putString(SnapshotKey, payload.toString()).apply()
    }

    fun restore(player: Player): Boolean {
        val raw = preferences.getString(SnapshotKey, null) ?: return false
        val snapshot = runCatching { JSONObject(raw) }.getOrNull() ?: return false
        val version = snapshot.optInt("version", -1)
        if (version !in 1..SnapshotVersion) return false

        val queueJson = snapshot.optJSONArray("queue") ?: return false
        val items = buildList {
            for (index in 0 until queueJson.length()) {
                val item = queueJson.optJSONObject(index) ?: continue
                val id = item.optLong("id", 0L)
                if (id == 0L) continue

                val storedUri = item.optString("uri")
                val uri = if (storedUri.isNotBlank()) {
                    Uri.parse(storedUri)
                } else if (id > 0L) {
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id,
                    )
                } else {
                    continue
                }

                add(
                    MediaItem.Builder()
                        .setMediaId(id.toString())
                        .setUri(uri)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(item.optString("title"))
                                .setArtist(item.optString("artist"))
                                .setAlbumTitle(item.optString("album"))
                                .setAlbumArtist(
                                    item.optString("albumArtist")
                                        .takeIf { it.isNotBlank() }
                                )
                                .setGenre(
                                    item.optString("genre")
                                        .takeIf { it.isNotBlank() }
                                )
                                .setArtworkUri(
                                    item.optString("artworkUri")
                                        .takeIf { it.isNotBlank() }
                                        ?.let(Uri::parse)
                                )
                                .setExtras(
                                    Bundle().apply {
                                        putString(SourceUriExtraKey, uri.toString())
                                        item.optInt("bitrateBps", 0)
                                            .takeIf { it > 0 }
                                            ?.let { putInt(BitrateExtraKey, it) }
                                        item.optInt("sampleRateHz", 0)
                                            .takeIf { it > 0 }
                                            ?.let { putInt(SampleRateExtraKey, it) }
                                    }
                                )
                                .build()
                        )
                        .build()
                )
            }
        }

        if (items.isEmpty()) return false

        val index = snapshot.optInt("index", 0).coerceIn(items.indices)
        val positionMs = snapshot.optLong("positionMs", 0L).coerceAtLeast(0L)

        player.repeatMode = snapshot
            .optInt("repeatMode", Player.REPEAT_MODE_OFF)
            .takeIf {
                it == Player.REPEAT_MODE_OFF ||
                    it == Player.REPEAT_MODE_ONE ||
                    it == Player.REPEAT_MODE_ALL
            }
            ?: Player.REPEAT_MODE_OFF
        player.shuffleModeEnabled = snapshot.optBoolean("shuffle", false)
        player.setMediaItems(items, index, positionMs)
        player.prepare()
        player.playWhenReady = false
        return true
    }

    fun clear() {
        preferences.edit().remove(SnapshotKey).apply()
    }
}
