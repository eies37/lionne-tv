package com.lionne.tv.feature.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaylistUriTest {
    @Test fun acceptsOnlinePlaylists() {
        assertEquals("example.org", playlistUri("https://example.org/playlist.m3u?key=value")?.host)
        assertEquals("localhost", playlistUri("http://localhost:8080/live")?.host)
    }

    @Test fun rejectsUnsupportedOrMalformedAddresses() {
        listOf(
            "file:///tmp/list.m3u",
            "https:///list.m3u",
            "https://user:pass@example.org/list.m3u",
            "https://exam ple.org/list.m3u",
        ).forEach { assertNull(it, playlistUri(it)) }
    }
}
