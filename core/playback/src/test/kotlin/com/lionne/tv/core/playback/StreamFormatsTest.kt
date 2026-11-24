package com.lionne.tv.core.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StreamFormatsTest {
    @Test
    fun `detection du format par l'adresse`() {
        assertEquals(StreamFormats.HLS, StreamFormats.mimeTypeFor("https://x/live.isml/.m3u8?token=a"))
        assertEquals(StreamFormats.DASH, StreamFormats.mimeTypeFor("https://x/Manifest.MPD"))
        assertNull(StreamFormats.mimeTypeFor("https://x/film.mp4"))
        assertNull(StreamFormats.mimeTypeFor("http://x/chaine.ts"))
    }
}
