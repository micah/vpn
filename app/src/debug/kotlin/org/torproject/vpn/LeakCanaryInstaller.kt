package org.torproject.vpn

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import leakcanary.AppWatcher


class LeakCanaryInstaller : ContentProvider() {
    override fun onCreate(): Boolean {
        if (!isTest) {
            AppWatcher.manualInstall(context!!.applicationContext as Application)
        }
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }

    private val isTest: Boolean
        get() = try {
            Class.forName("org.junit.Test")
            true
        } catch (e: java.lang.Exception) {
            false
        }
}
