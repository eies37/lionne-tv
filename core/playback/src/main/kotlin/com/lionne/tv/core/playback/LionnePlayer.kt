package com.lionne.tv.core.playback

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.lionne.tv.core.model.MediaEntry

/**
 * Lecteur de Lionne TV, basé sur Media3 / ExoPlayer.
 * Gère HLS (.m3u8), DASH (.mpd) et les fichiers progressifs (MP4…).
 *
 * Les écrans n'utilisent que cette classe : changer de moteur de lecture
 * ne toucherait que ce module.
 */
class LionnePlayer(context: Context) {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build()

    val positionMs: Long get() = exoPlayer.currentPosition

    /** Durée en millisecondes, ou -1 si inconnue (direct). */
    val durationMs: Long get() = exoPlayer.duration.takeIf { it != C.TIME_UNSET } ?: -1L

    fun play(entry: MediaEntry, startPositionMs: Long = 0L) {
        val builder = MediaItem.Builder()
            .setUri(entry.streamUrl)
            .setMediaId(entry.id)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(entry.title).build())
        StreamFormats.mimeTypeFor(entry.streamUrl)?.let { builder.setMimeType(it) }
        exoPlayer.setMediaItem(builder.build(), startPositionMs.coerceAtLeast(0L))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    /** Avance (delta positif) ou recule (delta négatif) dans une vidéo à la demande. */
    fun seekBy(deltaMs: Long) {
        val duration = durationMs
        val target = (exoPlayer.currentPosition + deltaMs).coerceAtLeast(0L)
        exoPlayer.seekTo(if (duration > 0) target.coerceAtMost(duration) else target)
    }

    /** Notifie [onError] avec un message lisible à chaque erreur de lecture. */
    fun onError(onError: (String) -> Unit): Player.Listener {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                onError("Lecture impossible (${error.errorCodeName})")
            }
        }
        exoPlayer.addListener(listener)
        return listener
    }

    fun release() {
        exoPlayer.release()
    }
}

/** Détection du format à partir de l'adresse, quand le serveur ne le précise pas. */
object StreamFormats {
    const val HLS = "application/x-mpegURL"
    const val DASH = "application/dash+xml"

    fun mimeTypeFor(url: String): String? {
        val path = url.substringBefore('?').substringBefore('#').lowercase()
        return when {
            path.endsWith(".m3u8") || path.endsWith(".m3u") -> HLS
            path.endsWith(".mpd") -> DASH
            else -> null
        }
    }
}
