package com.lionne.tv.feature.settings

import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.lionne.tv.core.data.SourceDescriptor
import com.lionne.tv.core.data.UserPlaylistStore
import java.net.URI
import java.util.UUID

@Composable
fun SettingsScreen(store: UserPlaylistStore, onChanged: () -> Unit, onBack: () -> Unit) {
    var sources by remember(store) { mutableStateOf(store.read()) }
    var addOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val backFocus = remember { FocusRequester() }

    fun save(updated: List<SourceDescriptor>) {
        store.save(updated)
        sources = updated
        onChanged()
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 48.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, modifier = Modifier.focusRequester(backFocus)) { Text("Retour") }
            Text("Listes M3U", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { addOpen = true }) { Text("Ajouter une liste") }
        }
        if (sources.isEmpty()) Text("Aucune liste personnelle configurée.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sources, key = { it.id }) { source ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(source.name, modifier = Modifier.weight(1f), maxLines = 1)
                    Button(onClick = {
                        save(sources.map { if (it.id == source.id) it.copy(enabled = !it.enabled) else it })
                    }) { Text(if (source.enabled) "Désactiver" else "Activer") }
                    Button(onClick = {
                        backFocus.requestFocus()
                        save(sources.filterNot { it.id == source.id })
                    }) { Text("Retirer") }
                }
            }
        }
    }
    LaunchedEffect(Unit) { backFocus.requestFocus() }
    if (addOpen) {
        androidx.compose.runtime.DisposableEffect(context) {
            val input = EditText(context).apply {
                hint = "https://exemple.fr/liste.m3u"
                inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI
                imeOptions = EditorInfo.IME_ACTION_DONE
                setSingleLine(true)
            }
            val dialog = android.app.AlertDialog.Builder(context)
                .setTitle("Adresse de la liste M3U")
                .setView(input)
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Ajouter", null)
                .create()
            dialog.setOnDismissListener { addOpen = false }
            dialog.show()
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val address = input.text.toString().trim()
                val uri = playlistUri(address)
                if (uri == null || sources.any { it.location == address }) {
                    input.error = if (uri == null) "Saisissez une adresse HTTP ou HTTPS valide." else "Cette liste existe déjà."
                } else {
                    save(sources + SourceDescriptor("m3u", "user-${UUID.randomUUID()}", uri.host, address))
                    dialog.dismiss()
                }
            }
            onDispose {
                dialog.setOnDismissListener(null)
                dialog.dismiss()
            }
        }
    }
}

internal fun playlistUri(address: String): URI? =
    runCatching { URI(address) }.getOrNull()?.takeIf {
        it.scheme?.lowercase() in setOf("http", "https") && !it.host.isNullOrBlank() && it.rawUserInfo == null
    }
