package com.rezoxnemesis.muse.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MuseBackupRepository(
    private val context: Context,
    private val preferences: MusePreferences,
) {
    suspend fun exportBackup(destination: Uri): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = preferences.exportBackupJson()
                context.contentResolver.openOutputStream(
                    destination,
                    "wt",
                )?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
                    writer.write(json)
                } ?: error("Could not open the selected backup destination.")
            }
        }

    suspend fun restoreBackup(
        source: Uri,
    ): Result<MuseBackupSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = context.contentResolver.openInputStream(source)
                ?.use { input ->
                    val output = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(8 * 1024)
                    var total = 0

                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        require(total <= MaxBackupBytes) {
                            "Muse backup is too large."
                        }
                        output.write(buffer, 0, read)
                    }
                    output.toByteArray()
                }
                ?: error("Could not open the selected backup.")

            val text = bytes.toString(Charsets.UTF_8)
                .removePrefix("\uFEFF")
                .trim()

            require(text.isNotBlank()) {
                "The selected backup is empty."
            }

            preferences.restoreBackupJson(text)
        }
    }

    private companion object {
        const val MaxBackupBytes = 2 * 1024 * 1024
    }
}
