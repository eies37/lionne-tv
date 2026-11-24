package com.lionne.tv.source.m3u

import com.lionne.tv.core.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class M3uParserTest {

    @Test
    fun `liste etendue avec attributs, virgule dans le titre et fins de ligne Windows`() {
        val text = "﻿#EXTM3U\r\n" +
            "#EXTINF:-1 tvg-id=\"info.fr\" tvg-logo=\"https://x/logo.png\" group-title=\"Info, actualités\",Lionne Info, 24h/24\r\n" +
            "https://x/info.m3u8\r\n" +
            "\r\n" +
            "#EXTINF:-1 group-title=\"VOD Films\",Un film\r\n" +
            "https://x/film.m3u8\r\n" +
            "#EXTINF:0,Sans groupe\r\n" +
            "#EXTGRP:Sport\r\n" +
            "http://x/sport.ts\r\n"

        val entries = M3uParser.parse(text, "liste")

        assertEquals(3, entries.size)
        val info = entries[0]
        assertEquals("Lionne Info, 24h/24", info.title)
        assertEquals("Info, actualités", info.category)
        assertEquals("https://x/logo.png", info.logoUrl)
        assertEquals("info.fr", info.epgId)
        assertEquals(ContentType.LIVE, info.type)
        assertEquals("liste", info.sourceId)
        assertEquals(true, info.id.startsWith("liste:info.fr@"))

        assertEquals(ContentType.VOD, entries[1].type)
        assertEquals("Sport", entries[2].category)
        assertNull(entries[2].logoUrl)
    }

    @Test
    fun `url seule, doublons et fichiers video`() {
        val text = """
            #EXTM3U
            https://x/chaine.m3u8
            https://x/chaine.m3u8
            #EXTINF:-1,Film
            https://x/films/mon-film.mp4?token=1
        """.trimIndent()

        val entries = M3uParser.parse(text, "l")

        assertEquals(2, entries.size)
        assertEquals("chaine", entries[0].title)
        assertEquals(M3uParser.DEFAULT_CATEGORY, entries[0].category)
        assertEquals(ContentType.VOD, entries[1].type)
    }

    @Test
    fun `identifiants stables d'un chargement a l'autre`() {
        val text = "#EXTM3U\n#EXTINF:-1,A\nhttps://x/a.m3u8\n"
        assertEquals(M3uParser.parse(text, "l").single().id, M3uParser.parse(text, "l").single().id)
    }

    @Test
    fun `guides annonces dans l'en-tete`() {
        val text = "#EXTM3U url-tvg=\"https://x/a.xml.gz, https://x/b.xml\" x-tvg-url=\"https://x/a.xml.gz\" tvg-shift=\"1\"\n" +
            "#EXTINF:-1,A\nhttps://x/a.m3u8\n"
        assertEquals(listOf("https://x/a.xml.gz", "https://x/b.xml"), M3uParser.guideLocations(text))
        assertEquals(emptyList<String>(), M3uParser.guideLocations("#EXTINF:-1,A\nhttps://x/a.m3u8"))
    }
}
