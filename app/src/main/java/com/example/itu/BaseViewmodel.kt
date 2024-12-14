package com.example.itu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.exp
import kotlin.reflect.KClass

abstract class BaseViewmodel : ViewModel() {

    val apiUrl: String = "http://192.168.0.177:5000"

    abstract fun fetchData()

    protected fun <T: Any> getRequest(url: String, resultClass: KClass<T>, dataName: String, getArray: Boolean = false, debug: Boolean = false): T {
        Log.d("Get Start", url)
        val connection = URL(apiUrl + url).openConnection() as HttpURLConnection

        if (connection.responseCode != 200) {
            Log.d("Response: ", connection.responseCode.toString() + " - " + connection.responseMessage)
            return resultClass.java.newInstance()
        }
        val inputSystem = connection.inputStream
        val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
        if(debug) {
            Log.d("Response: ", inputStreamReader.readText())
        }
        val json = Gson().fromJson(inputStreamReader, JsonObject::class.java)

        val parsedData = Gson().fromJson(json.get(dataName), resultClass.java)

        inputStreamReader.close()
        inputSystem.close()
        Log.d("Get Done", url)
        return parsedData
    }
    suspend fun <T: Any>  PostRequest(url: String, sentObject: T, expectResponse: Boolean = false) : String
    {
        Log.d("request: ", "POST " + url)
        val connection = URL(apiUrl + url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-type", "application/json; charset=utf-8")
        val serializedObject = Gson().toJson(sentObject)
        connection.outputStream.write(serializedObject.toByteArray())
        Log.d("response: ", connection.responseCode.toString() + " - " + connection.responseMessage)
        Log.d("JSON", serializedObject)
        if(connection.responseCode == 200 && expectResponse)
        {
            val inputSystem = connection.inputStream
            val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
//            Log.d("Json response", inputStreamReader.readText())
            return inputStreamReader.readText()
        }
        return ""
    }
    suspend fun <T: Any> PutRequest(url: String, sentObject: T) {
        Log.d("request: ", "PUT " + url)
        val connection = URL(apiUrl + url).openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-type", "application/json; charset=utf-8")
        val serializedObject = Gson().toJson(sentObject)
        connection.outputStream.write(serializedObject.toByteArray())
        Log.d("response: ", connection.responseCode.toString() + " - " + connection.responseMessage)


    }
    suspend fun DeleteRequest(url: String) {
        Log.d("request: ", "DELETE " + url)
        val connection = URL(apiUrl + url).openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        Log.d("response: ", connection.responseCode.toString() + " - " + connection.responseMessage)
    }

    fun <T: Any> ParseJson(json: String, result: KClass<T>, dataName: String): T
    {
        val parsedJson = Gson().fromJson(json, JsonObject::class.java)

        val parsedData = Gson().fromJson(parsedJson.get(dataName), result.java)

        return parsedData
    }
}