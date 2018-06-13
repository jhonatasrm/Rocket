package org.mozilla.rocket.pwa

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.mozilla.focus.tabs.Tab
import org.mozilla.focus.utils.IOUtils
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.MalformedURLException
import java.net.URL

interface PwaViewContract {
    fun notifyPwaReady(tab: Tab, jsonObject: PwaModel)
}

class PwaModel(
        val short_name: String,
        val name: String,
        val display: String,
        val icon: Icon?,
        val start_url: String,
        val theme_color: String,
        val background_color: String) {

    val pwaIconName: String by lazy {
        if (short_name.isNotEmpty())
            return@lazy short_name
        if (name.isNotEmpty())
            return@lazy name
        if (start_url.isNotEmpty())
            return@lazy start_url
        ""

    }
    var iconBitmap: Bitmap? = null


    data class Icon(
            val src: String,
            val sizes: String,
            val type: String)

    object iconParser {
        fun findMax(jsonArray: JSONArray): Icon? {
            val list: ArrayList<Icon> = ArrayList()
            for (i in 0..jsonArray.length() - 1) {
                val jsonObject = jsonArray.getJSONObject(i)
                val icon = Icon(jsonObject.optString("src"),
                        jsonObject.optString("sizes"),
                        jsonObject.optString("type"))
                list.add(icon)
            }
            var size = 0
            var max: Icon? = null

            for (icon in list) {
                val s = icon.sizes.split("x")[0]
                try {
                    val valueOf = Integer.valueOf(s)
                    if (valueOf > size) {
                        size = valueOf
                        max = icon
                    }
                } catch (e: NumberFormatException) {
                    continue
                }

            }
            return max
        }
    }

    companion object {
        fun fromJson(pwaUrl: String, pwaJsonObject: JSONObject): PwaModel {

            val icons: Icon?
            if (pwaJsonObject.has("icons")) {
                icons = iconParser.findMax(pwaJsonObject.getJSONArray("icons"))
            } else {
                icons = null
            }

            val shortName = pwaJsonObject.optString("short_name")
            val name = pwaJsonObject.optString("name")
            val display = pwaJsonObject.optString("display")
            val link = pwaJsonObject.optString("start_url", null)
                    ?: throw IllegalStateException(" no start_url in manifest")
            val startUrl = PwaPresenter.link(link, pwaUrl)
                    ?: throw IllegalStateException(" can't map start_url to a valid url")


            val theme_color = pwaJsonObject.optString("theme_color")
            val background_color = pwaJsonObject.optString("background_color")
            return PwaModel(shortName,
                    name,
                    display,
                    icons,
                    startUrl,
                    theme_color,
                    background_color)
        }
    }


}

class PwaPresenter(val tab: Tab, viewContract: PwaViewContract) : LifecycleObserver {

    private var pwaViewContract: WeakReference<PwaViewContract> = WeakReference(viewContract)
    private var job: Job? = null

    // cancel the job if it won't be displayed.
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun cancelJob() {
        job?.cancel()
    }

    companion object {

        const val NOT_PWA = "null"

        fun link(link: String, url: String): String? {
            var result = ""
            var linkIsValidUrl: Boolean
            try {
                URL(link)
                linkIsValidUrl = true
            } catch (e: MalformedURLException) {
                linkIsValidUrl = false
            }
            try {
                val URL = URL(url)
                var path = URL.path.substring(0, URL.path.lastIndexOf('/') + 1)
                if (path.length == 0) {
                    path = "/";
                }

                when {
                    linkIsValidUrl -> result = link
                    link.equals(".") -> result = URL.protocol + "://" + URL.host + path
                    link.startsWith(".") -> result = URL.protocol + "://" + URL.host + path + link
                    link.startsWith("?") -> result = url + link
                    link.startsWith("/") -> result = URL.protocol + "://" + URL.host + link
                    else -> result = URL.protocol + "://" + URL.host + path + link
                }
            } catch (e: MalformedURLException) {
                return null
            }

            return result

        }

    }

    fun bindTab(host: String, result: String) {

        val path = result.replace("\"", "")

        if (path == PwaPresenter.NOT_PWA) return

        job = launch(UI) {
            try {

                val pwaModel = getPwaModel(host, path)
                pwaViewContract.get()?.notifyPwaReady(tab, pwaModel)
            } catch (e: MalformedURLException) {
            } catch (e: IllegalStateException) {
            } catch (e: JSONException) {
            } catch (e: IOException) {
                Log.d("PWA", "bindTab failed:: " + e)
            }
        }


    }

    private suspend fun getPwaModel(url: String, pwaPath: String): PwaModel = withContext(CommonPool) {
        val manifestUrl = link(pwaPath, url)
                ?: throw IllegalStateException("Can't get PWA Manifest")

        val tempUrl = URL(manifestUrl)
        val pwaJson = IOUtils.readUrl(tempUrl)
        val pwaModel = PwaModel.fromJson(manifestUrl, pwaJson)
        if (pwaModel.icon != null) {
            val src = pwaModel.icon.src
            val iconUrl = link(src, manifestUrl) ?: return@withContext pwaModel
            val bmp = BitmapFactory.decodeStream(URL(iconUrl).openStream())
            pwaModel.iconBitmap = bmp
        }
        pwaModel
    }

}