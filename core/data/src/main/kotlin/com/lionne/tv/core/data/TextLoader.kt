package com.lionne.tv.core.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Charge le texte d'une ressource désignée par une adresse (`https://…`, `asset://…`, etc.). */
fun interface TextLoader {
    suspend fun load(location: String): String
}

/**
 * Aiguille chaque adresse vers le chargeur de son schéma (`http`, `https`, `asset`…).
 * L'application fournit les chargeurs propres à Android (fichiers embarqués).
 */
class CompositeTextLoader(private val byScheme: Map<String, TextLoader>) : TextLoader {
    override suspend fun load(location: String): String {
        val scheme = location.substringBefore("://", missingDelimiterValue = "").lowercase()
        val loader = byScheme[scheme]
            ?: throw IOException("Aucun chargeur pour l'adresse « $location »")
        return loader.load(location)
    }
}

/** Téléchargement HTTP(S) sans dépendance externe, pour garder l'application légère. */
class HttpTextLoader(
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 20_000,
    private val maxBytes: Int = 20 * 1024 * 1024,
) : TextLoader {
    override suspend fun load(location: String): String = withContext(Dispatchers.IO) {
        val connection = URL(location).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "LionneTV/0.1")
            val code = connection.responseCode
            if (code !in 200..299) throw IOException("HTTP $code pour « $location »")
            connection.inputStream.use { input ->
                val bytes = input.readNBytesCompat(maxBytes)
                String(bytes.gunzipIfNeeded(maxBytes * GZIP_RATIO), Charsets.UTF_8)
            }
        } finally {
            connection.disconnect()
        }
    }
}

private const val GZIP_RATIO = 5

/** Les guides XMLTV sont souvent servis compressés (`.xml.gz`). */
internal fun ByteArray.gunzipIfNeeded(limit: Int): ByteArray {
    val isGzip = size >= 2 && this[0] == 0x1f.toByte() && this[1] == 0x8b.toByte()
    if (!isGzip) return this
    return java.util.zip.GZIPInputStream(inputStream()).use { it.readNBytesCompat(limit) }
}

private fun java.io.InputStream.readNBytesCompat(limit: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(16 * 1024)
    var total = 0
    while (true) {
        val read = read(buffer)
        if (read < 0) break
        total += read
        if (total > limit) throw IOException("Fichier trop volumineux (> ${limit / 1024 / 1024} Mo)")
        out.write(buffer, 0, read)
    }
    return out.toByteArray()
}
