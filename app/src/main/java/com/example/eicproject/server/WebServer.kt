package com.example.eicproject.server

import android.content.Context
import android.util.Log
import com.example.eicproject.database.PlantData
import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.util.Scanner
import java.util.concurrent.Executors

class WebServer {

    private val context : Context
    private var sentData : List<PlantData>? = null
    private var sentImage : ByteArray? = null

    constructor(context : Context) {
        this.context = context
    }

    private var mHttpServer: HttpServer? = null

    fun streamToString(inputStream: InputStream): String {
        val s = Scanner(inputStream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

    fun setNewSentData(data : List<PlantData>) {
        this.sentData = data
    }

    fun setNewImage(img : ByteArray) {
        this.sentImage = img
    }

    fun startServer(port: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                mHttpServer = HttpServer.create(InetSocketAddress(port), 0)
                mHttpServer!!.executor = Executors.newCachedThreadPool()
                mHttpServer!!.createContext("/", rootHandler)
                // 'this' refers to the handle method
                mHttpServer!!.createContext("/data", dataHandler)
                mHttpServer!!.createContext("/image.jpg", imageHandler)
                mHttpServer!!.start()//start server;
                Log.i("test", "Server is running on ${mHttpServer!!.address}:$port")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Handler for root endpoint
    private val rootHandler = HttpHandler { exchange ->
        run {
            // Get request method
            when (exchange!!.requestMethod) {
                "GET" -> {
                    val inStream : InputStream = context.assets.open("web.html")
                    val array : ByteArray = readAllBytes(inStream)
                    exchange.sendResponseHeaders(200, array.size.toLong())
                    val os = exchange.responseBody
                    os.write(array)
                    os.close()
                    //sendResponse(exchange, "Welcome to my server")
                }
            }
        }
    }

    // Handler for data endpoint
    private val dataHandler = HttpHandler { httpExchange ->
        run {
            when (httpExchange!!.requestMethod) {
                "GET" -> {
                    val jsonObj : JSONObject = JSONObject()
                    jsonObj.put("smth" , "123")
                    val gson : Gson = Gson()
                    val parsed : String = gson.toJson(sentData)
                    Log.i("test", parsed)
                    sendResponse(httpExchange, parsed)
                }
            }
        }
    }

    // Handler for data endpoint
    private val imageHandler = HttpHandler { httpExchange ->
        run {
            when (httpExchange!!.requestMethod) {
                "GET" -> {
                    if (sentImage != null) {
                        val length : Long = sentImage!!.size.toLong()
                        httpExchange.sendResponseHeaders(200, length)
                        val os = httpExchange.responseBody
                        os.write(sentImage)
                        os.close()
                        Log.i("test", "sent to locaiton ye")
                    }
                }
            }
        }
    }

    private fun sendResponse(httpExchange: HttpExchange, responseText: String) {
        httpExchange.sendResponseHeaders(200, responseText.length.toLong())
        val os = httpExchange.responseBody
        os.write(responseText.toByteArray())
        os.close()
    }

    @Throws(IOException::class)
    fun readAllBytes(`in`: InputStream): ByteArray {
        val out = ByteArrayOutputStream()
        copyAllBytes(`in`, out)
        return out.toByteArray()
    }

    /**
     * Copies all available data from in to out without closing any stream.
     *
     * @return number of bytes copied
     */
    @Throws(IOException::class)
    fun copyAllBytes(`in`: InputStream, out: OutputStream): Int {
        var byteCount = 0
        val buffer = ByteArray(4096)
        while (true) {
            val read = `in`.read(buffer)
            if (read == -1) {
                break
            }
            out.write(buffer, 0, read)
            byteCount += read
        }
        return byteCount
    }
}