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
import kotlin.reflect.KClass

abstract class BaseViewmodel : ViewModel() {

    abstract fun fetchData()

    protected fun <T: Any> getRequest(url: String, resultClass: KClass<T>, dataName: String, getArray: Boolean = false): T {
        val connection = URL(url).openConnection() as HttpURLConnection

        if (connection.responseCode != 200) {
            Log.d("Response: ", connection.responseCode.toString())
            return resultClass.java.newInstance()
        }
        val inputSystem = connection.inputStream
        val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")

        val json = Gson().fromJson(inputStreamReader, JsonObject::class.java)

        val parsedData = Gson().fromJson(json.get(dataName), resultClass.java)

        inputStreamReader.close()
        inputSystem.close()

        return parsedData
    }
    suspend fun <T: Any> PostRequest(url: String, sentObject: T)
    {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-type", "application/json; charset=utf-8")
                val serializedObject = Gson().toJson(sentObject)
                connection.outputStream.write(serializedObject.toByteArray())
                Log.d("response: ", connection.responseCode.toString() + " - " + connection.responseMessage)
            }
        }
    }
    suspend fun <T: Any> PutRequest(url: String, sentObject: T) {

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-type", "application/json; charset=utf-8")
        val serializedObject = Gson().toJson(sentObject)
        connection.outputStream.write(serializedObject.toByteArray())
        Log.d("response: ", connection.responseCode.toString() + " - " + connection.responseMessage)


    }
}