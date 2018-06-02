package org.mozilla.rocket.pwa

import org.junit.Assert.assertEquals
import org.junit.Test

class URLTest {

    @Test
    fun `test relative path`() {

        // link start with . as relative path
        assertLink("http://www.mozilla.org/",
                ".",
                "http://www.mozilla.org/")

        // link start with . as relative path
        assertLink("http://www.mozilla.org/b/./a.html",
                "./a.html",
                "http://www.mozilla.org/b/c.json")

        // link to page with url end without '/' will stay at the same directory
        assertLink("http://www.mozilla.org/a.html",
                "a.html",
                "http://www.mozilla.org")


        // link to page with url end with '/' will stay at the same directory
        assertLink("http://www.mozilla.org/a.html",
                "a.html",
                "http://www.mozilla.org/")


        // url with path should work
        assertLink("http://www.mozilla.org/b/a.html",
                "a.html",
                "http://www.mozilla.org/b/c.html")


    }


    @Test
    fun `test absolute path`() {

        // link start with '/', and url ends without '/'
        assertLink("https://app.ft.com/metatags.json?v=3",
                "/metatags.json?v=3",
                "https://app.ft.com/index_page/home")

        // link start with '/', and url ends with '/'
         assertLink("https://www.englishaccentsmap.com/manifest.json",
                "/manifest.json",
                "https://www.englishaccentsmap.com/")

        // link start with '/', and url ends with '/'
        assertLink("http://www.mozilla.org/a/b.html",
                "/a/b.html",
                "http://www.mozilla.org/c/d.html")
    }

    @Test
    fun `invalid url will return null`() {

        assertLink(null,
                "/a/b.html",
                "www.mozilla.org/c/d.html"
        )
    }

    @Test
    fun `invalid link will return null`() {

        assertLink("http://www.mozilla.org/c/d.html?aaa/b.html",
                "?aaa/b.html",
                "http://www.mozilla.org/c/d.html"
        )
    }


    @Test
    fun `use the link directly if it is a valid URL`() {

        val link = "http://link.mozilla.org/a.html"
        assertLink(link,
                link,
                "http://target.mozilla.org/b/c/d.html"
        )
    }




    fun assertLink(expect: String?, link: String, url: String) {
        assertEquals(expect, PwaPresenter.link(link, url))
    }


}