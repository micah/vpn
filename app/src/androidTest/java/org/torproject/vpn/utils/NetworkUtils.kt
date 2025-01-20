package org.torproject.vpn.utils

import org.json.JSONObject
import java.io.*
import java.net.URL
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

object NetworkUtils {

    fun getGeoIPLocale(): String? {
        val responseJson = getJsonFromGeoIPService()
        if (responseJson.has("YourFuckingCountry")) {
            return  responseJson.getString("YourFuckingCountry") as String
        }

        return null
    }

    fun getExitIP(): String? {
        val responseJson = getJsonFromGeoIPService()
        if (responseJson.has("YourFuckingIPAddress")) {
            return responseJson.getString("YourFuckingIPAddress") as String
        }
        return null
    }

   /**
     * Calls wtfismyip as geoip service.
     * For some reason there are no public sslCertificates assigned the the default SSL socket factory
     * in the instrumentation test. As a work-around we've pinned the corresponding X509 certs.
     */
    private fun getJsonFromGeoIPService(): JSONObject {
        val url = URL("https://wtfismyip.com/json")
        val connection = url.openConnection() as HttpsURLConnection
        connection.sslSocketFactory = createSslSocketFactoryWithCertificateChain("wtfismyip.pem")
        val response = StringBuilder()

        try {
            val streamReader = InputStreamReader(BufferedInputStream(connection.inputStream))
            readResponse(streamReader, response)
        } catch (exception: RuntimeException) {
            exception.printStackTrace()
        } finally {
            connection.disconnect()
        }

        return try {
            JSONObject(response.toString().trim())
        } catch (exception: java.lang.Exception) {
            exception.printStackTrace()
            JSONObject()
        }
    }

    /**
     * Creates a SSLSocketFactory with pinned certificates
     */
    private fun createSslSocketFactoryWithCertificateChain(vararg certificateNames: String): SSLSocketFactory {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        // Initialize an empty KeyStore
        keyStore.load(null, null)

        val cf = CertificateFactory.getInstance("X.509")

        // Load each certificate in the chain
        for ((index, name) in certificateNames.withIndex()) {
            val inputStream: InputStream? = javaClass.classLoader?.getResourceAsStream(name)
            inputStream?.use { caInput ->
                val ca: X509Certificate = cf.generateCertificate(caInput) as X509Certificate
                keyStore.setCertificateEntry("cert$index", ca)
            }
        }

        // Create a TrustManager that trusts the CAs in our KeyStore
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(keyStore)

        // Create an SSLContext that uses our TrustManager
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, tmf.trustManagers, null)

        return sslContext.socketFactory
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