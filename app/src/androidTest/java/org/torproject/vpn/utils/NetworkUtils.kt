package org.torproject.vpn.utils

import org.json.JSONObject
import java.io.*
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

object NetworkUtils {

    fun getGeoIPLocale(): String? {
        val url = URL("https://wtfismyip.com/json")
        val connection = url.openConnection() as HttpsURLConnection
        val response = StringBuilder()

        try {
            val streamReader = InputStreamReader(BufferedInputStream(connection.inputStream))
            readResponse(streamReader, response)
        } catch (exception: RuntimeException) {
            exception.printStackTrace()
        } finally {
            connection.disconnect()
        }

        val responseJson: JSONObject;
        try {
            responseJson = JSONObject(response.toString().trim())
        } catch (exception: java.lang.Exception) {
            exception.printStackTrace()
            return null
        }

        if (responseJson.has("YourFuckingCountry")) {
            return  responseJson.getString("YourFuckingCountry") as String
        }

        return null
    }

    @Throws(IOException::class)
    private fun readResponse(reader: InputStreamReader, log: StringBuilder?) {
        val buf = CharArray(10)
        var read = 0
        try {
            while (reader.read(buf).also { read = it } != -1) {
                log?.append(buf, 0, read)
            }
        } catch (e: IOException) {
            reader.close()
            throw IOException(e)
        }
        reader.close()
    }

}