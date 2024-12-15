/**
 * @author Michael Peštuka (xpestu01)
 */

package com.example.itu

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.reflect.KClass

abstract class BaseViewmodel : ViewModel() {

    // URL of backend server
    val apiUrl: String = "http://192.168.0.177:5000"

    abstract fun fetchData()

    /**
     * Sends a get request to server and returns JSON data parsed to specified class
     * @param url request URL
     * @param resultClass class type that will be returned
     * @param getArray true if expected data is an array of classes
     * @param dataName name of data in JSON
     */
    protected fun <T: Any> getRequest(url: String, resultClass: KClass<T>, dataName: String, getArray: Boolean = false): T {
        // Start connection
        val connection = URL(apiUrl + url).openConnection() as HttpURLConnection

        // When request fails return empty instance
        if (connection.responseCode != 200) {
            return resultClass.java.newInstance()
        }

        // Read and parse JSON
        val inputSystem = connection.inputStream
        val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
        val json = Gson().fromJson(inputStreamReader, JsonObject::class.java)
        val parsedData = Gson().fromJson(json.get(dataName), resultClass.java)
        inputStreamReader.close()
        inputSystem.close()

        return parsedData
    }

    /**
     * Sends a post request containing class instance data
     * @param url request URL
     * @param sentObject class that will be parsed into JSOn and sent
     * @param expectResponse true if a response is expected
     * @return String JSON response from server
     */
    protected fun <T: Any>  postRequest(url: String, sentObject: T, expectResponse: Boolean = false) : String
    {
        // Start connection
        val connection = URL(apiUrl + url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-type", "application/json; charset=utf-8")

        // Parse object into JSON and send
        val serializedObject = Gson().toJson(sentObject)
        connection.outputStream.write(serializedObject.toByteArray())

        // Return response if successful
        if(connection.responseCode == 200 && expectResponse)
        {
            val inputSystem = connection.inputStream
            val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
            return inputStreamReader.readText()
        }
        // If response not expected or request failed
        return ""
    }

    /**
     * Sends a put request containing class data
     * @param url request URL
     * @param sentObject class that will be parsed into JSON and sent
     */
    protected fun <T: Any> putRequest(url: String, sentObject: T) {
        val connection = URL(apiUrl + url).openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-type", "application/json; charset=utf-8")
        val serializedObject = Gson().toJson(sentObject)
        connection.outputStream.write(serializedObject.toByteArray())
    }

    /**
     * Send a delete request to server
     * @param url request URL
     */
    protected fun deleteRequest(url: String) {
        Log.d("request: ", "DELETE " + url)
        val connection = URL(apiUrl + url).openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        Log.d("response: ", connection.responseCode.toString() + " - " + connection.responseMessage)
    }

    /**
     * Parses JSON into a class instance
     * @param json JSON data
     * @param result Class type to be returned
     * @param dataName JSON name of data
     */
    fun <T: Any> parseJson(json: String, result: KClass<T>, dataName: String): T
    {
        val parsedJson = Gson().fromJson(json, JsonObject::class.java)

        val parsedData = Gson().fromJson(parsedJson.get(dataName), result.java)

        return parsedData
    }
}