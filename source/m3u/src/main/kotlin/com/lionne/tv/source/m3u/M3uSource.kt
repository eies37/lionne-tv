package com.lionne.tv.source.m3u

import com.lionne.tv.core.data.SourceDescriptor
import com.lionne.tv.core.data.SourceFactory
import com.lionne.tv.core.data.TextLoader
import com.lionne.tv.core.model.ContentSource
import com.lionne.tv.core.model.ContentType
import com.lionne.tv.core.model.MediaEntry

/** Liste IPTV au format M3U / M3U8 étendu (`#EXTM3U`, `#EXTINF`, `group-title`, `tvg-logo`…). */
class M3uSource(
    override val id: String,
    override val name: String,
    private val location: String,
    private val loader: TextLoader,
) : ContentSource {
    @Volatile
    private var announcedGuides: List<String> = emptyList()

    override val guideLocations: List<String> get() = announcedGuides

    override suspend fun load(): List<MediaEntry> {
        val text = loader.load(location)
        announcedGuides = M3uParser.guideLocations(text)
        return M3uParser.parse(text, id)
    }
}

/** Fabrique enregistrée sous le type `m3u` dans `sources.json`. */
object M3uSourceFactory : SourceFactory {
    const val TYPE = "m3u"

    override fun create(descriptor: SourceDescriptor, loader: TextLoader): ContentSource =
        M3uSource(descriptor.id, descriptor.name, descriptor.location, loader)
}

object M3uParser {
    const val DEFAULT_CATEGORY = "Chaînes"

    private val attributeRegex = Regex("""([A-Za-z0-9_-]+)="([^"]*)"""")
    private val vodExtensions = listOf(".mp4", ".mkv", ".avi", ".mov", ".webm", ".m4v")

    fun parse(text: String, sourceId: String): List<MediaEntry> {
        val entries = mutableListOf<MediaEntry>()
        val seenUrls = HashSet<String>()
        var pendingInfo: ExtInf? = null
        var pendingGroup: String? = null

        for (rawLine in text.removePrefix("﻿").lineSequence()) {
            val line = rawLine.trim()
            when {
                line.isEmpty() -> Unit
                line.startsWith("#EXTINF", ignoreCase = true) -> pendingInfo = parseExtInf(line)
                line.startsWith("#EXTGRP:", ignoreCase = true) -> pendingGroup = line.substringAfter(':').trim()
                line.startsWith("#") -> Unit
                else -> {
                    val url = line
                    if (seenUrls.add(url)) {
                        val info = pendingInfo
                        val category = info?.attributes?.get("group-title")?.takeIf { it.isNotBlank() }
                            ?: pendingGroup?.takeIf { it.isNotBlank() }
                            ?: DEFAULT_CATEGORY
                        entries += MediaEntry(
                            id = "$sourceId:${stableKey(info, url)}",
                            title = info?.title?.takeIf { it.isNotBlank() }
                                ?: info?.attributes?.get("tvg-name")?.takeIf { it.isNotBlank() }
                                ?: titleFromUrl(url),
                            streamUrl = url,
                            type = guessType(url, info),
                            category = category,
                            sourceId = sourceId,
                            logoUrl = info?.attributes?.get("tvg-logo")?.takeIf { it.isNotBlank() },
                            epgId = info?.attributes?.get("tvg-id")?.takeIf { it.isNotBlank() },
                        )
                    }
                    pendingInfo = null
                    pendingGroup = null
                }
            }
        }
        return entries
    }

    /** Guides annoncés dans l'en-tête : `#EXTM3U url-tvg="a.xml.gz,b.xml"` ou `x-tvg-url`. */
    fun guideLocations(text: String): List<String> {
        val header = text.removePrefix("\uFEFF").lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
            ?.takeIf { it.startsWith("#EXTM3U", ignoreCase = true) }
            ?: return emptyList()
        return attributeRegex.findAll(header)
            .filter { it.groupValues[1].lowercase() in setOf("url-tvg", "x-tvg-url") }
            .flatMap { it.groupValues[2].split(',') }
            .map { it.trim() }
            .filter { it.startsWith("http://") || it.startsWith("https://") }
            .distinct()
            .toList()
    }

    internal data class ExtInf(val title: String, val attributes: Map<String, String>)

    /** `#EXTINF:-1 tvg-id="x" group-title="Info",Nom, avec virgule` → titre après la 1re virgule hors guillemets. */
    internal fun parseExtInf(line: String): ExtInf {
        val body = line.substringAfter(':', "")
        var inQuotes = false
        var commaIndex = -1
        for ((i, c) in body.withIndex()) {
            if (c == '"') inQuotes = !inQuotes
            if (c == ',' && !inQuotes) { commaIndex = i; break }
        }
        val header = if (commaIndex >= 0) body.substring(0, commaIndex) else body
        val title = if (commaIndex >= 0) body.substring(commaIndex + 1).trim() else ""
        val attributes = attributeRegex.findAll(header).associate { it.groupValues[1].lowercase() to it.groupValues[2].trim() }
        return ExtInf(title, attributes)
    }

    private fun guessType(url: String, info: ExtInf?): ContentType {
        val path = url.substringBefore('?').lowercase()
        if (vodExtensions.any { path.endsWith(it) }) return ContentType.VOD
        val group = info?.attributes?.get("group-title")?.lowercase().orEmpty()
        if (group.startsWith("vod") || group.contains("film") || group.contains("movie")) return ContentType.VOD
        return ContentType.LIVE
    }

    private fun stableKey(info: ExtInf?, url: String): String =
        info?.attributes?.get("tvg-id")?.takeIf { it.isNotBlank() }?.let { "$it@${hash(url)}" } ?: hash(url)

    private fun hash(value: String): String = value.hashCode().toUInt().toString(16)

    private fun titleFromUrl(url: String): String =
        url.substringBefore('?').trimEnd('/').substringAfterLast('/').substringBeforeLast('.').ifBlank { url }
}
