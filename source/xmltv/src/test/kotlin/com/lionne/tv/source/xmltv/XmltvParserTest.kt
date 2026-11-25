package com.lionne.tv.source.xmltv

import com.lionne.tv.core.data.GuideRepository
import com.lionne.tv.core.model.GuideSource
import com.lionne.tv.core.model.Programme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class XmltvParserTest {

    private val sample = """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE tv SYSTEM "xmltv.dtd">
        <tv generator-info-name="test">
          <channel id="Info.fr"><display-name>Lionne Info</display-name></channel>
          <programme start="20261124180000 +0100" stop="20261124190000 +0100" channel="Info.fr">
            <title lang="fr">Le Journal</title>
            <title lang="en">The News</title>
            <desc lang="fr">Toute l'actualité &amp; la météo</desc>
          </programme>
          <programme start="20261124190000 +0100" stop="20261124203000 +0100" channel="Info.fr">
            <title>Le Débat</title>
          </programme>
          <programme start="20261124190000" channel="Sport.fr">
            <title>Match</title>
          </programme>
          <programme start="invalide" channel="Sport.fr"><title>Ignoré</title></programme>
        </tv>
    """.trimIndent()

    @Test
    fun `lecture des emissions, des fuseaux et des entites`() {
        val programmes = XmltvParser.parse(sample)

        assertEquals(3, programmes.size)
        val journal = programmes[0]
        assertEquals("Info.fr", journal.channelId)
        assertEquals("Le Journal", journal.title)
        assertEquals("Toute l'actualité & la météo", journal.description)
        assertEquals(XmltvParser.parseTime("20261124170000 +0000"), journal.startMs)
        assertEquals(60 * 60_000L, journal.endMs - journal.startMs)
        // Sans heure de fin : 30 minutes par défaut.
        assertEquals(30 * 60_000L, programmes[2].endMs - programmes[2].startMs)
    }

    @Test
    fun `heures XMLTV`() {
        assertEquals(1_795_539_600_000L, XmltvParser.parseTime("20261124170000 +0000"))
        assertEquals(1_795_539_600_000L, XmltvParser.parseTime("20261124180000 +0100"))
        assertEquals(1_795_539_600_000L, XmltvParser.parseTime("20261124120000 -0500"))
        assertNull(XmltvParser.parseTime("2026"))
        assertNull(XmltvParser.parseTime(null))
    }

    @Test
    fun `fenetre de temps`() {
        val from = XmltvParser.parseTime("20261124183000 +0100")!!
        val to = XmltvParser.parseTime("20261124184500 +0100")!!
        assertEquals(listOf("Le Journal"), XmltvParser.parse(sample, from, to).map { it.title })
    }

    @Test
    fun `emission en cours et suivante, sans tenir compte des majuscules`() = runBlocking {
        val guide = GuideRepository(
            listOf(
                object : GuideSource {
                    override val id = "g"
                    override val name = "Guide"
                    override suspend fun load(): List<Programme> = XmltvParser.parse(sample)
                },
            ),
        )
        assertEquals(emptyList<Any>(), guide.refresh())

        val at1830 = XmltvParser.parseTime("20261124183000 +0100")!!
        val nowNext = guide.nowNext("info.FR", at1830)
        assertEquals("Le Journal", nowNext?.now?.title)
        assertEquals("Le Débat", nowNext?.next?.title)
        assertNull(guide.nowNext("inconnue", at1830))
        assertNull(guide.nowNext(null, at1830))
    }
}
