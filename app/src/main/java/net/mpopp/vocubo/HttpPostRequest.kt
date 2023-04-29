package net.mpopp.vocubo

import android.os.AsyncTask
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpPostRequest : AsyncTask<Void?, Void?, String?> {
    private val url: String
    private val parameters: MutableMap<String, String>
    private val callback: HttpPostRequestCallback?
    private val timeout: Int

    constructor(callback: HttpPostRequestCallback?, url: String) {
        this.callback = callback
        this.url = url
        parameters = HashMap()

        // default timeout is 5000 milliseconds
        timeout = 5000
    }

    constructor(callback: HttpPostRequestCallback?, url: String, timeout: Int) {
        this.callback = callback
        this.url = url
        parameters = HashMap()
        this.timeout = timeout
    }

    fun setParameter(key: String, value: String) {
        parameters[key] = value
    }

    override fun doInBackground(vararg params: Void?): String? {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var reader: BufferedReader? = null
        var result: String? = null
        try {
            val url = URL(url)
            connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept-Charset", "UTF-8")
            connection!!.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = timeout
            connection.readTimeout = timeout
            val outputStream = connection.outputStream
            val builder = StringBuilder()
            for ((key, value) in parameters) {
                builder.append(key).append("=").append(value).append("&")
            }
            var parameters = builder.toString()
            parameters = parameters.substring(0, parameters.length - 1)
            outputStream.write(parameters.toByteArray())
            outputStream.flush()
            outputStream.close()
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.inputStream
                reader = BufferedReader(InputStreamReader(inputStream))
                val responseBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    responseBuilder.append(line)
                }
                result = responseBuilder.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    override fun onPostExecute(result: String?) {
        callback?.onRequestComplete(result)
    }

    interface HttpPostRequestCallback {
        fun onRequestComplete(result: String?)
    }
}