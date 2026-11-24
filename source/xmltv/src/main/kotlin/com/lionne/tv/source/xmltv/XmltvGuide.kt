package com.lionne.tv.source.xmltv

import com.lionne.tv.core.data.GuideFactory
import com.lionne.tv.core.data.SourceDescriptor
import com.lionne.tv.core.data.TextLoader
import com.lionne.tv.core.model.GuideSource
import com.lionne.tv.core.model.Programme
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.io.StringReader
import java.util.GregorianCalendar
import java.util.TimeZone
import javax.xml.parsers.SAXParserFactory

/** Guide des programmes au format XMLTV (`<tv><programme start stop channel>…`). */
class XmltvGuide(
    override val id: String,
    override val name: String,
    private val location: String,
    private val loader: TextLoader,
    private val clock: () -> Long = System::currentTimeMillis,
) : GuideSource {
    override suspend fun load(): List<Programme> {
        val now = clock()
        // On ne garde qu'une fenêtre utile pour limiter la mémoire : 6 h avant, 48 h après.
        return XmltvParser.parse(loader.load(location), fromMs = now - 6 * HOUR_MS, toMs = now + 48 * HOUR_MS)
    }

    private companion object {
        const val HOUR_MS = 3_600_000L
    }
}

/** Fabrique enregistrée sous le type `xmltv` dans la section `guides` de `sources.json`. */
object XmltvGuideFactory : GuideFactory {
    const val TYPE = "xmltv"

    override fun create(descriptor: SourceDescriptor, loader: TextLoader): GuideSource =
        XmltvGuide(descriptor.id, descriptor.name, descriptor.location, loader)
}

object XmltvParser {

    /** Lit les émissions qui chevauchent [fromMs, toMs[. */
    fun parse(text: String, fromMs: Long = Long.MIN_VALUE, toMs: Long = Long.MAX_VALUE): List<Programme> {
        val handler = Handler(fromMs, toMs)
        val factory = SAXParserFactory.newInstance().apply { isNamespaceAware = false }
        // Pas de chargement de DTD ni d'entités externes (sécurité et hors ligne).
        runCatching { factory.setFeature("http://xml.org/sax/features/external-general-entities", false) }
        runCatching { factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
        runCatching { factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false) }
        factory.newSAXParser().parse(InputSource(StringReader(text.removePrefix("﻿"))), handler)
        return handler.programmes
    }

    /** `20261124190000 +0100` → millisecondes UTC. Sans décalage, l'heure est prise en UTC. */
    fun parseTime(raw: String?): Long? {
        val t = raw?.trim().orEmpty()
        if (t.length < 12 || !t.take(12).all { it.isDigit() }) return null
        val calendar = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                t.substring(0, 4).toInt(),
                t.substring(4, 6).toInt() - 1,
                t.substring(6, 8).toInt(),
                t.substring(8, 10).toInt(),
                t.substring(10, 12).toInt(),
                if (t.length >= 14 && t[12].isDigit()) t.substring(12, 14).toInt() else 0,
            )
        }
        var millis = calendar.timeInMillis
        val offset = t.substringAfter(' ', "").trim()
        if (offset.length == 5 && (offset[0] == '+' || offset[0] == '-') && offset.drop(1).all { it.isDigit() }) {
            val sign = if (offset[0] == '-') -1 else 1
            val minutes = offset.substring(1, 3).toInt() * 60 + offset.substring(3, 5).toInt()
            millis -= sign * minutes * 60_000L
        }
        return millis
    }

    private class Handler(private val fromMs: Long, private val toMs: Long) : DefaultHandler() {
        val programmes = mutableListOf<Programme>()

        private var channel: String? = null
        private var start: Long? = null
        private var stop: Long? = null
        private var title: String? = null
        private var desc: String? = null
        private val text = StringBuilder()
        private var capturing = false

        override fun resolveEntity(publicId: String?, systemId: String?): InputSource =
            InputSource(StringReader(""))

        override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
            when (qName) {
                "programme" -> {
                    channel = attributes.getValue("channel")
                    start = parseTime(attributes.getValue("start"))
                    stop = parseTime(attributes.getValue("stop"))
                    title = null
                    desc = null
                }
                "title", "desc" -> if (channel != null) {
                    text.setLength(0)
                    capturing = true
                }
            }
        }

        override fun characters(ch: CharArray, startIndex: Int, length: Int) {
            if (capturing) text.append(ch, startIndex, length)
        }

        override fun endElement(uri: String?, localName: String?, qName: String) {
            when (qName) {
                "title" -> if (capturing) {
                    if (title == null) title = text.toString().trim()
                    capturing = false
                }
                "desc" -> if (capturing) {
                    if (desc == null) desc = text.toString().trim().ifEmpty { null }
                    capturing = false
                }
                "programme" -> {
                    val c = channel
                    val s = start
                    val name = title
                    if (c != null && s != null && !name.isNullOrBlank()) {
                        val e = stop?.takeIf { it > s } ?: (s + DEFAULT_DURATION_MS)
                        if (e > fromMs && s < toMs) {
                            programmes += Programme(c, name, s, e, desc)
                        }
                    }
                    channel = null
                }
            }
        }
    }

    private const val DEFAULT_DURATION_MS = 30 * 60_000L
}
