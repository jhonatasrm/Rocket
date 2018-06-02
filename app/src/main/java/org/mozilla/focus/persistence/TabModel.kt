package org.mozilla.focus.persistence

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import org.json.JSONObject
import org.mozilla.rocket.pwa.PwaModel

@Entity(tableName = "tabs")
class TabModel(@field:PrimaryKey
               @field:ColumnInfo(name = "tab_id")
               var id: String, @field:ColumnInfo(name = "tab_parent_id")
               var parentId: String?, @field:ColumnInfo(name = "tab_title")
               var title: String?, @field:ColumnInfo(name = "tab_url")
               var url: String?) {

    /**
     * PWA info for this tab
     */
    @Ignore
    var pwa: PwaModel? = null

    /**
     * Thumbnail bitmap for tab previewing.
     */
    @Ignore
    var thumbnail: Bitmap? = null

    /**
     * Favicon bitmap for tab tray item.
     */
    @Ignore
    var favicon: Bitmap? = null

    /**
     * ViewState for this Tab. Usually to fill by WebView.saveViewState(Bundle)
     * Set it as @Ignore to avoid storing this field into database.
     * It will be serialized to a file and save the uri path into webViewStateUri field.
     */
    @Ignore
    var webViewState: Bundle? = null

    val isValid: Boolean
        get() {
            val hasId = !TextUtils.isEmpty(this.id)
            val hasUrl = !TextUtils.isEmpty(this.url)

            return hasId && hasUrl
        }

    @Ignore
    constructor(id: String, parentId: String) : this(id, parentId, "", "") {
    }

    override fun toString(): String {
        return "TabModel{" +
                "id='" + id + '\''.toString() +
                ", parentId='" + parentId + '\''.toString() +
                ", title='" + title + '\''.toString() +
                ", url='" + url + '\''.toString() +
                ", thumbnail=" + thumbnail +
                ", favicon=" + favicon +
                ", webViewState=" + webViewState +
                '}'.toString()
    }
}
