package com.lionne.tv.feature.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.lionne.tv.core.data.NowNext
import com.lionne.tv.core.data.PlaybackPositionStore
import com.lionne.tv.core.data.ResumePolicy
import com.lionne.tv.core.data.TimeFormat
import com.lionne.tv.core.data.Zapping
import com.lionne.tv.core.designsystem.LocalBrand
import com.lionne.tv.core.model.ContentType
import com.lionne.tv.core.model.MediaEntry
import com.lionne.tv.core.playback.LionnePlayer
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lecture plein écran.
 * - Direct : ▲/▼ ou CH+/CH− pour changer de chaîne, OK pour afficher le bandeau.
 * - À la demande : OK lecture/pause, ◀ ▶ 10 s, position affichée, reprise là où on s'était arrêté.
 */
@Composable
fun PlayerScreen(
    entry: MediaEntry,
    liveChannels: List<MediaEntry>,
    positions: PlaybackPositionStore,
    onBack: () -> Unit,
    nowNext: (MediaEntry) -> NowNext? = { null },
) {
    val context = LocalContext.current
    val lionnePlayer = remember { LionnePlayer(context) }
    var current by remember { mutableStateOf(entry) }
    var error by remember { mutableStateOf<String?>(null) }
    var overlayRequest by remember { mutableIntStateOf(0) }
    var overlayVisible by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val latest by rememberUpdatedState(current)

    fun saveVodPosition() {
        val playing = latest
        if (playing.type != ContentType.VOD) return
        val toStore = ResumePolicy.positionToStore(lionnePlayer.positionMs, lionnePlayer.durationMs)
        if (toStore == null) positions.clear(playing.id) else positions.save(playing.id, toStore)
    }

    DisposableEffect(Unit) {
        val listener = lionnePlayer.onError { message -> error = message }
        onDispose {
            lionnePlayer.exoPlayer.removeListener(listener)
            saveVodPosition()
            lionnePlayer.release()
        }
    }

    LaunchedEffect(current.id) {
        error = null
        val start = if (current.type == ContentType.VOD) positions.get(current.id) else 0L
        lionnePlayer.play(current, start)
        overlayRequest++
    }

    LaunchedEffect(overlayRequest) {
        overlayVisible = true
        delay(OVERLAY_DURATION_MS)
        overlayVisible = false
    }

    // Position de lecture des vidéos, rafraîchie tant que le bandeau est affiché.
    LaunchedEffect(overlayVisible, current.id) {
        progress = null
        while (overlayVisible && current.type == ContentType.VOD) {
            val position = TimeFormat.format(lionnePlayer.positionMs)
            val duration = lionnePlayer.durationMs.takeIf { it > 0 }?.let(TimeFormat::format) ?: "--:--"
            val paused = if (lionnePlayer.exoPlayer.isPlaying || lionnePlayer.exoPlayer.playWhenReady) "" else " · En pause"
            progress = "$position / $duration$paused"
            delay(PROGRESS_REFRESH_MS)
        }
    }

    BackHandler {
        saveVodPosition()
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                if (current.type == ContentType.VOD) {
                    handleVodKey(event.key, lionnePlayer).also { handled -> if (handled) overlayRequest++ }
                } else {
                    when (event.key) {
                        Key.DirectionUp, Key.ChannelUp -> {
                            Zapping.move(liveChannels, current.id, +1)?.let { current = it }
                            true
                        }
                        Key.DirectionDown, Key.ChannelDown -> {
                            Zapping.move(liveChannels, current.id, -1)?.let { current = it }
                            true
                        }
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                            overlayRequest++
                            true
                        }
                        else -> false
                    }
                }
            },
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = lionnePlayer.exoPlayer
                    useController = false
                    keepScreenOn = true
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        val message = error
        if (overlayVisible || message != null) {
            InfoOverlay(
                entry = current,
                guide = remember(current.id, overlayRequest) { nowNext(current) },
                progress = progress,
                error = message,
                showZapHint = current.type == ContentType.LIVE && liveChannels.size > 1,
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
    }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }
}

@Composable
private fun InfoOverlay(
    entry: MediaEntry,
    guide: NowNext?,
    progress: String?,
    error: String?,
    showZapHint: Boolean,
    modifier: Modifier = Modifier,
) {
    val brand = LocalBrand.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))))
            .padding(horizontal = 48.dp, vertical = 27.dp),
    ) {
        Text(
            text = if (entry.type == ContentType.LIVE) "EN DIRECT · ${entry.category}" else entry.category,
            style = MaterialTheme.typography.labelLarge,
            color = brand.primary,
        )
        Text(text = entry.title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
        guide?.now?.let { now ->
            Text(
                text = "Maintenant · ${hhmm(now.startMs)}–${hhmm(now.endMs)} · ${now.title}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
            )
        }
        guide?.next?.let { next ->
            Text(
                text = "Ensuite · ${hhmm(next.startMs)} · ${next.title}",
                style = MaterialTheme.typography.bodyMedium,
                color = brand.onSurfaceMuted,
                maxLines = 1,
            )
        }
        entry.description?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium, color = brand.onSurfaceMuted, maxLines = 2)
        }
        if (progress != null) {
            Text(text = progress, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(
                text = "OK : lecture / pause   ◀ ▶ : 10 s",
                style = MaterialTheme.typography.bodySmall,
                color = brand.onSurfaceMuted,
            )
        }
        if (error != null) {
            Text(text = error, style = MaterialTheme.typography.bodyMedium, color = brand.accent)
        }
        if (showZapHint) {
            Text(
                text = "▲ ▼ : changer de chaîne",
                style = MaterialTheme.typography.bodySmall,
                color = brand.onSurfaceMuted,
            )
        }
    }
}

/** Télécommande en vidéo à la demande : OK = lecture/pause, ◀ ▶ = recul/avance de 10 s. */
private fun handleVodKey(key: Key, player: LionnePlayer): Boolean =
    when (key) {
        Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.MediaPlayPause -> {
            player.togglePlayPause()
            true
        }
        Key.MediaPlay -> {
            player.exoPlayer.play()
            true
        }
        Key.MediaPause -> {
            player.exoPlayer.pause()
            true
        }
        Key.DirectionLeft, Key.MediaRewind -> {
            player.seekBy(-SEEK_STEP_MS)
            true
        }
        Key.DirectionRight, Key.MediaFastForward -> {
            player.seekBy(SEEK_STEP_MS)
            true
        }
        Key.DirectionUp, Key.DirectionDown -> true
        else -> false
    }

/** Heure locale de la TV, format 24 h. */
private fun hhmm(epochMs: Long): String =
    SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date(epochMs))

private const val OVERLAY_DURATION_MS = 4_000L
private const val SEEK_STEP_MS = 10_000L
private const val PROGRESS_REFRESH_MS = 500L
