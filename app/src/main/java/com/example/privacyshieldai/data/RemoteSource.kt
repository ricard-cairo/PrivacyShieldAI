package com.example.privacyshieldai.data

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

/**
 * Descarga el catálogo remoto. Se usa HttpURLConnection para no añadir dependencias;
 * si se porta a Kotlin Multiplatform, sustituir por Ktor Client.
 */
object RemoteSource {

    private const val TIMEOUT_MS = 10_000
    private const val MAX_BYTES = 512 * 1024 // el catálogo pesa unos pocos KB

    @Throws(IOException::class)
    fun fetch(url: String): String {
        require(url.startsWith("https://")) { "Solo se permiten URLs https" }
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("Accept", "application/json")

            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) throw IOException("HTTP $code")

            val text = connection.inputStream.use { input ->
                val buffer = ByteArray(8 * 1024)
                val output = ByteArrayOutputStream()
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    if (output.size() > MAX_BYTES) throw IOException("Respuesta demasiado grande")
                }
                output.toString(Charsets.UTF_8.name())
            }
            return text
        } finally {
            connection.disconnect()
        }
    }
}
