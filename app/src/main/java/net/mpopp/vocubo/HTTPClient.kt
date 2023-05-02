package net.mpopp.vocubo

import kotlinx.coroutines.*
import java.io.*
import java.net.*
import java.nio.charset.StandardCharsets

class HttpClient(private val url: String) {

    companion object {
        private const val DEFAULT_TIMEOUT = 5000 // milliseconds
    }

    interface Callback {
        fun onSuccess(response: String)
        fun onError(e: Exception)
    }

    private var timeout = DEFAULT_TIMEOUT

    fun setTimeout(seconds: Int) {
        timeout = seconds * 1000 // convert seconds to milliseconds
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun post(params: Map<String, String>, callback: Callback) {
        val postData = buildPostData(params)
        val contentType = "application/x-www-form-urlencoded"
        val contentLength = postData.length

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", contentType)
                connection.setRequestProperty("Content-Length", contentLength.toString())
                connection.connectTimeout = timeout
                connection.readTimeout = timeout

                val output = DataOutputStream(connection.outputStream)
                output.writeBytes(postData)
                output.flush()
                output.close()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = BufferedReader(
                        InputStreamReader(
                            connection.inputStream,
                            StandardCharsets.UTF_8
                        )
                    )
                    val response = StringBuffer()
                    var inputLine: String?
                    while (inputStream.readLine().also { inputLine = it } != null) {
                        response.append(inputLine)
                    }
                    inputStream.close()

                    withContext(Dispatchers.Main) {
                        callback.onSuccess(response.toString())
                    }
                } else {
                    throw Exception("HTTP error code: $responseCode")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError(e)
                }
            }
        }
    }

    private fun buildPostData(params: Map<String, String>): String {
        val builder = StringBuilder()
        for ((key, value) in params) {
            if (builder.isNotEmpty()) {
                builder.append("&")
            }
            builder.append(URLEncoder.encode(key, "UTF-8"))
            builder.append("=")
            builder.append(URLEncoder.encode(value, "UTF-8"))
        }
        return builder.toString()
    }
}
